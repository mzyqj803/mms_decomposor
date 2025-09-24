package com.mms.service.impl;

import com.mms.entity.Containers;
import com.mms.entity.ContainerComponents;
import com.mms.entity.Contracts;
import com.mms.entity.ContractParameters;
import com.mms.repository.ContainersRepository;
import com.mms.repository.ContainerComponentsRepository;
import com.mms.repository.ContainersComponentsSummaryRepository;
import com.mms.repository.ContainerComponentsBreakdownRepository;
import com.mms.repository.ContractsRepository;
import com.mms.service.CacheService;
import com.mms.service.ContainerUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerUploadServiceImpl implements ContainerUploadService {
    
    private final ContainersRepository containersRepository;
    private final ContainerComponentsRepository containerComponentsRepository;
    private final ContainersComponentsSummaryRepository containersComponentsSummaryRepository;
    private final ContainerComponentsBreakdownRepository containerComponentsBreakdownRepository;
    private final ContractsRepository contractsRepository;
    private final CacheService cacheService;
    
    @Override
    public Map<String, Object> previewExcelFile(MultipartFile file) {
        try {
            // 解析Excel文件
            List<ContainerData> containerDataList = parseExcelFile(file);
            
            // 构建预览数据
            Map<String, Object> previewData = new HashMap<>();
            previewData.put("success", true);
            previewData.put("containerCount", containerDataList.size());
            previewData.put("containers", containerDataList);
            
            // 计算总组件数量
            int totalComponents = containerDataList.stream()
                .mapToInt(data -> data.getComponents().size())
                .sum();
            previewData.put("totalComponents", totalComponents);
            
            log.info("Excel文件预览成功: 装箱单数量={}, 组件总数={}", containerDataList.size(), totalComponents);
            return previewData;
            
        } catch (Exception e) {
            log.error("预览Excel文件失败", e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("success", false);
            errorData.put("message", "预览失败: " + e.getMessage());
            return errorData;
        }
    }
    
    @Override
    @Transactional
    public List<Containers> parseAndCreateContainers(Long contractId, MultipartFile file) {
        try {
            // 获取合同
            Contracts contract = contractsRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("合同不存在"));
            
            // 清除当前合同下的所有相关数据
            clearContractData(contractId);
            
            // 解析Excel文件
            List<ContainerData> containerDataList = parseExcelFile(file);
            
            // 创建装箱单（createContainer方法内部已经处理保存逻辑）
            List<Containers> savedContainers = new ArrayList<>();
            for (ContainerData data : containerDataList) {
                Containers container = createContainer(contract, data);
                savedContainers.add(container);
            }
            
            log.info("成功解析并创建装箱单: 合同ID={}, 装箱单数量={}", contractId, savedContainers.size());
            
            // 清除相关合同的缓存
            clearContractContainersCache(contractId);
            
            return savedContainers;
            
        } catch (Exception e) {
            log.error("解析装箱单文件失败: 合同ID={}", contractId, e);
            throw new RuntimeException("解析装箱单文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Containers> findSimilarContainers(Long contractId) {
        try {
            // 获取当前合同
            Contracts currentContract = contractsRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("合同不存在"));
            
            // 获取当前合同的参数
            List<ContractParameters> currentParams = currentContract.getParameters();
            if (currentParams == null || currentParams.isEmpty()) {
                log.info("合同ID={}没有参数，无法查找相似装箱单", contractId);
                return new ArrayList<>();
            }
            
            // 查找所有其他合同的装箱单
            List<Containers> allContainers = containersRepository.findAll();
            List<Containers> similarContainers = new ArrayList<>();
            
            for (Containers container : allContainers) {
                // 跳过当前合同的装箱单
                if (container.getContract().getId().equals(contractId)) {
                    continue;
                }
                
                // 检查合同参数相似度
                if (isSimilarContract(container.getContract(), currentContract)) {
                    similarContainers.add(container);
                }
            }
            
            log.info("为合同ID={}找到{}个相似装箱单", contractId, similarContainers.size());
            return similarContainers;
            
        } catch (Exception e) {
            log.error("查找相似装箱单失败: 合同ID={}", contractId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 判断两个合同是否相似
     */
    private boolean isSimilarContract(Contracts contract1, Contracts contract2) {
        List<ContractParameters> params1 = contract1.getParameters();
        List<ContractParameters> params2 = contract2.getParameters();
        
        if (params1 == null || params2 == null) {
            return false;
        }
        
        // 计算参数匹配度
        int matchCount = 0;
        int totalParams = Math.max(params1.size(), params2.size());
        
        for (ContractParameters param1 : params1) {
            for (ContractParameters param2 : params2) {
                if (param1.getParamName().equals(param2.getParamName()) &&
                    param1.getParamValue().equals(param2.getParamValue())) {
                    matchCount++;
                    break;
                }
            }
        }
        
        // 如果匹配度超过50%，认为是相似合同
        return totalParams > 0 && (double) matchCount / totalParams >= 0.5;
    }
    
    @Override
    @Transactional
    public List<Containers> cloneContainers(Long sourceContractId, Long targetContractId) {
        // 验证源合同和目标合同存在
        contractsRepository.findById(sourceContractId)
            .orElseThrow(() -> new RuntimeException("源合同不存在"));
        Contracts targetContract = contractsRepository.findById(targetContractId)
            .orElseThrow(() -> new RuntimeException("目标合同不存在"));
        
        // 获取源合同的装箱单
        List<Containers> sourceContainers = containersRepository.findByContractId(sourceContractId);
        
        // 克隆装箱单（cloneContainer方法内部已经处理保存逻辑）
        List<Containers> savedContainers = new ArrayList<>();
        for (Containers sourceContainer : sourceContainers) {
            Containers clonedContainer = cloneContainer(sourceContainer, targetContract);
            savedContainers.add(clonedContainer);
        }
        
        log.info("成功克隆装箱单: 源合同ID={}, 目标合同ID={}, 装箱单数量={}", 
            sourceContractId, targetContractId, savedContainers.size());
        
        // 清除目标合同的缓存（因为目标合同的数据发生了变化）
        clearContractContainersCache(targetContractId);
        
        return savedContainers;
    }
    /**
     * 按照装箱单sample解析文件
     * 装箱单格式：
     * 1. 顶部有设备号和项目名称
     * 2. 每个箱号（如BNO-1-1 1/2）包含一个物料清单
     * 3. 每个物料清单有表头：序号、代号、名称、单位、数量、备注
     * 4. 物料数据按行排列
     */
    private List<ContainerData> parseExcelFile(MultipartFile file) throws IOException {
        List<ContainerData> containerDataList = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            String deviceNo = null;
            String projectName = null;
            String currentContainerNo = null;
            String currentContainerType = null;
            List<ComponentData> currentComponents = new ArrayList<>();
            
            // 遍历所有行
            for (Row row : sheet) {
                if (row == null) continue;
                
                // 检查是否是设备号和项目名称行
                String firstCellValue = getCellStringValue(row.getCell(0));
                if (firstCellValue != null && firstCellValue.contains("设备号")) {
                    // 设备号在C列（索引2）
                    String deviceNoValue = getCellStringValue(row.getCell(2));
                    if (deviceNoValue != null && !deviceNoValue.trim().isEmpty()) {
                        deviceNo = deviceNoValue.trim();
                    }
                    continue;
                }
                
                if (firstCellValue != null && firstCellValue.contains("项目名称")) {
                    // 项目名称在F列（索引5）
                    String projectNameValue = getCellStringValue(row.getCell(5));
                    if (projectNameValue != null && !projectNameValue.trim().isEmpty()) {
                        projectName = projectNameValue.trim();
                    }
                    continue;
                }
                
                // 检查是否是箱号行（格式：箱号 BNO-1-1 1/2）
                if (firstCellValue != null && firstCellValue.startsWith("箱号")) {
                    // 保存上一个箱号的物料清单
                    if (currentContainerNo != null && !currentComponents.isEmpty()) {
                        ContainerData containerData = new ContainerData();
                        containerData.setContractNo(deviceNo);
                        containerData.setProjectName(projectName);
                        containerData.setContainerNo(currentContainerNo);
                        containerData.setContainerType(currentContainerType);
                        containerData.setComponents(new ArrayList<>(currentComponents));
                        containerDataList.add(containerData);
                    }
                    
                    // 箱包号从B列（索引1）提取
                    String containerNoValue = getCellStringValue(row.getCell(1));
                    if (containerNoValue != null && !containerNoValue.trim().isEmpty()) {
                        currentContainerNo = containerNoValue.trim();
                    }
                    
                    // 箱包名称从C列（索引2）提取
                    String containerTypeValue = getCellStringValue(row.getCell(2));
                    if (containerTypeValue != null && !containerTypeValue.trim().isEmpty()) {
                        currentContainerType = containerTypeValue.trim();
                    }
                    
                    currentComponents.clear();
                    continue;
                }
                
                // 检查是否是物料清单表头行
                if (firstCellValue != null && firstCellValue.equals("序号")) {
                    // 跳过表头行
                    continue;
                }
                
                // 检查是否是物料数据行（序号列是数字）
                if (firstCellValue != null && isNumeric(firstCellValue)) {
                    // 解析物料数据
                    ComponentData componentData = parseComponentRow(row);
                    if (componentData != null) {
                        currentComponents.add(componentData);
                    }
                }
            }
            
            // 保存最后一个箱号的物料清单
            if (currentContainerNo != null && !currentComponents.isEmpty()) {
                ContainerData containerData = new ContainerData();
                containerData.setContractNo(deviceNo);
                containerData.setProjectName(projectName);
                containerData.setContainerNo(currentContainerNo);
                containerData.setContainerType(currentContainerType);
                containerData.setComponents(new ArrayList<>(currentComponents));
                containerDataList.add(containerData);
            }
        }
        
        log.info("解析完成，共找到 {} 个箱号", containerDataList.size());
        return containerDataList;
    }
    
    /**
     * 解析物料数据行
     * 列结构：序号、代号、名称、单位、数量、备注
     */
    private ComponentData parseComponentRow(Row row) {
        try {
            String sequenceNo = getCellStringValue(row.getCell(0)); // 序号
            String componentNo = getCellStringValue(row.getCell(1)); // 代号
            String componentName = getCellStringValue(row.getCell(2)); // 名称
            String unitCode = getCellStringValue(row.getCell(3)); // 单位
            Integer quantity = getCellIntValue(row.getCell(4)); // 数量
            String comments = getCellStringValue(row.getCell(5)); // 备注
            
            // 验证必要字段
            if (componentNo == null || componentNo.trim().isEmpty()) {
                return null;
            }
            
            ComponentData componentData = new ComponentData();
            componentData.setSequenceNo(sequenceNo);
            componentData.setComponentNo(componentNo);
            componentData.setComponentName(componentName);
            componentData.setUnitCode(unitCode);
            componentData.setQuantity(quantity);
            componentData.setComments(comments);
            
            return componentData;
        } catch (Exception e) {
            log.warn("解析物料行失败，行号: {}", row.getRowNum(), e);
            return null;
        }
    }
    
    /**
     * 检查字符串是否为数字
     */
    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    private Integer getCellIntValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }
    
    private Containers createContainer(Contracts contract, ContainerData data) {
        Containers container = new Containers();
        container.setContract(contract);
        container.setContainerNo(data.getContainerNo());
        container.setName(data.getContainerType()); // 使用容器类型作为名称
        container.setComments(data.getProjectName()); // 将项目名称存储到备注中
        
        // 先保存装箱单
        Containers savedContainer = containersRepository.save(container);
        
        // 创建并保存组件
        List<ContainerComponents> components = new ArrayList<>();
        for (ComponentData compData : data.getComponents()) {
            ContainerComponents component = new ContainerComponents();
            component.setContainer(savedContainer);
            component.setComponentNo(compData.getComponentNo());
            component.setComponentName(compData.getComponentName());
            component.setUnitCode(compData.getUnitCode());
            component.setQuantity(compData.getQuantity());
            component.setComments(compData.getComments());
            components.add(component);
        }
        
        // 保存所有组件
        List<ContainerComponents> savedComponents = containerComponentsRepository.saveAll(components);
        savedContainer.setComponents(savedComponents);
        
        return savedContainer;
    }
    
    private Containers cloneContainer(Containers sourceContainer, Contracts targetContract) {
        Containers clonedContainer = new Containers();
        clonedContainer.setContract(targetContract);
        clonedContainer.setContainerNo(sourceContainer.getContainerNo());
        clonedContainer.setName(sourceContainer.getName());
        clonedContainer.setContainerSize(sourceContainer.getContainerSize());
        clonedContainer.setContainerWeight(sourceContainer.getContainerWeight());
        clonedContainer.setComments(sourceContainer.getComments());
        
        // 先保存装箱单
        Containers savedClonedContainer = containersRepository.save(clonedContainer);
        
        // 克隆组件
        List<ContainerComponents> clonedComponents = new ArrayList<>();
        if (sourceContainer.getComponents() != null) {
            for (ContainerComponents sourceComponent : sourceContainer.getComponents()) {
                ContainerComponents clonedComponent = new ContainerComponents();
                clonedComponent.setContainer(savedClonedContainer);
                clonedComponent.setComponentNo(sourceComponent.getComponentNo());
                clonedComponent.setComponentName(sourceComponent.getComponentName());
                clonedComponent.setUnitCode(sourceComponent.getUnitCode());
                clonedComponent.setQuantity(sourceComponent.getQuantity());
                clonedComponent.setComments(sourceComponent.getComments());
                clonedComponents.add(clonedComponent);
            }
        }
        
        // 保存所有克隆的组件
        List<ContainerComponents> savedComponents = containerComponentsRepository.saveAll(clonedComponents);
        savedClonedContainer.setComponents(savedComponents);
        
        return savedClonedContainer;
    }
    
    // 内部数据类
    private static class ContainerData {
        private String contractNo;
        private String projectName;
        private String containerNo;
        private String containerType;
        private List<ComponentData> components;
        
        // Getters and Setters
        
        public String getContractNo() { return contractNo; }
        public void setContractNo(String contractNo) { this.contractNo = contractNo; }
        
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        
        public String getContainerNo() { return containerNo; }
        public void setContainerNo(String containerNo) { this.containerNo = containerNo; }
        
        public String getContainerType() { return containerType; }
        public void setContainerType(String containerType) { this.containerType = containerType; }
        
        public List<ComponentData> getComponents() { return components; }
        public void setComponents(List<ComponentData> components) { this.components = components; }
    }
    
    private static class ComponentData {
        private String sequenceNo;
        private String componentNo;
        private String componentName;
        private String unitCode;
        private Integer quantity;
        private String comments;
        
        // Getters and Setters
        
        public String getSequenceNo() { return sequenceNo; }
        public void setSequenceNo(String sequenceNo) { this.sequenceNo = sequenceNo; }
        
        public String getComponentNo() { return componentNo; }
        public void setComponentNo(String componentNo) { this.componentNo = componentNo; }
        
        public String getComponentName() { return componentName; }
        public void setComponentName(String componentName) { this.componentName = componentName; }
        
        public String getUnitCode() { return unitCode; }
        public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }
    
    @Override
    public List<Containers> getContainersByContract(Long contractId) {
        return containersRepository.findByContractId(contractId);
    }
    
    @Override
    @Transactional
    public void deleteContainer(Long containerId) {
        Containers container = containersRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("装箱单不存在"));
        
        containersRepository.delete(container);
        log.info("删除装箱单: ID={}, 装箱单号={}", containerId, container.getContainerNo());
    }
    
    /**
     * 清除指定合同下的所有相关数据
     * 包括：containers, container_components, containers_components_summary, container_components_breakdown
     */
    @Transactional
    public void clearContractData(Long contractId) {
        try {
            log.info("开始清除合同ID={}的相关数据", contractId);
            
            // 删除分解记录
            containerComponentsBreakdownRepository.deleteByContractId(contractId);
            log.info("已删除合同ID={}的分解记录", contractId);
            
            // 删除汇总记录
            containersComponentsSummaryRepository.deleteByContractId(contractId);
            log.info("已删除合同ID={}的汇总记录", contractId);
            
            // 删除组件记录
            containerComponentsRepository.deleteByContractId(contractId);
            log.info("已删除合同ID={}的组件记录", contractId);
            
            // 删除装箱单记录
            containersRepository.deleteByContractId(contractId);
            log.info("已删除合同ID={}的装箱单记录", contractId);
            
            log.info("成功清除合同ID={}的所有相关数据", contractId);
            
        } catch (Exception e) {
            log.error("清除合同ID={}的数据失败", contractId, e);
            throw new RuntimeException("清除合同数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除指定合同的箱包缓存
     */
    private void clearContractContainersCache(Long contractId) {
        try {
            String cacheKey = "contract:containers:" + contractId;
            cacheService.delete(cacheKey);
            log.info("已清除合同ID={}的箱包缓存", contractId);
        } catch (Exception e) {
            log.warn("清除合同ID={}的箱包缓存失败: {}", contractId, e.getMessage());
        }
    }
}

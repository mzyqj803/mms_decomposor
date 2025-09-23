package com.mms.service.impl;

import com.mms.entity.Containers;
import com.mms.entity.ContainerComponents;
import com.mms.entity.Contracts;
import com.mms.repository.ContainersRepository;
import com.mms.repository.ContractsRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContainerUploadServiceImpl implements ContainerUploadService {
    
    private final ContainersRepository containersRepository;
    private final ContractsRepository contractsRepository;
    
    @Override
    @Transactional
    public List<Containers> parseAndCreateContainers(Long contractId, MultipartFile file) {
        try {
            // 获取合同
            Contracts contract = contractsRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("合同不存在"));
            
            // 解析Excel文件
            List<ContainerData> containerDataList = parseExcelFile(file);
            
            // 创建装箱单
            List<Containers> containers = new ArrayList<>();
            for (ContainerData data : containerDataList) {
                Containers container = createContainer(contract, data);
                containers.add(container);
            }
            
            // 保存到数据库
            List<Containers> savedContainers = containersRepository.saveAll(containers);
            
            log.info("成功解析并创建装箱单: 合同ID={}, 装箱单数量={}", contractId, savedContainers.size());
            return savedContainers;
            
        } catch (Exception e) {
            log.error("解析装箱单文件失败: 合同ID={}", contractId, e);
            throw new RuntimeException("解析装箱单文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Containers> findSimilarContainers(Long contractId) {
        // TODO: 根据合同参数查找类似的装箱单
        // 这里可以根据合同参数（如电梯类型、载重等）查找相似的装箱单
        return new ArrayList<>();
    }
    
    @Override
    @Transactional
    public List<Containers> cloneContainers(Long sourceContractId, Long targetContractId) {
        // 获取源合同和目标合同
        Contracts sourceContract = contractsRepository.findById(sourceContractId)
            .orElseThrow(() -> new RuntimeException("源合同不存在"));
        Contracts targetContract = contractsRepository.findById(targetContractId)
            .orElseThrow(() -> new RuntimeException("目标合同不存在"));
        
        // 获取源合同的装箱单
        List<Containers> sourceContainers = containersRepository.findByContractId(sourceContractId);
        
        // 克隆装箱单
        List<Containers> clonedContainers = new ArrayList<>();
        for (Containers sourceContainer : sourceContainers) {
            Containers clonedContainer = cloneContainer(sourceContainer, targetContract);
            clonedContainers.add(clonedContainer);
        }
        
        // 保存克隆的装箱单
        List<Containers> savedContainers = containersRepository.saveAll(clonedContainers);
        
        log.info("成功克隆装箱单: 源合同ID={}, 目标合同ID={}, 装箱单数量={}", 
            sourceContractId, targetContractId, savedContainers.size());
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
                    
                    // 解析箱号信息
                    String[] containerParts = firstCellValue.split(" ");
                    if (containerParts.length >= 2) {
                        currentContainerNo = containerParts[1]; // BNO-1-1
                    }
                    
                    // ContainerType从D列（索引3）提取
                    String containerTypeValue = getCellStringValue(row.getCell(3));
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
        
        // 创建组件
        List<ContainerComponents> components = new ArrayList<>();
        for (ComponentData compData : data.getComponents()) {
            ContainerComponents component = new ContainerComponents();
            component.setContainer(container);
            component.setComponentNo(compData.getComponentNo());
            component.setComponentName(compData.getComponentName());
            component.setUnitCode(compData.getUnitCode());
            component.setQuantity(compData.getQuantity());
            component.setComments(compData.getComments());
            components.add(component);
        }
        container.setComponents(components);
        
        return container;
    }
    
    private Containers cloneContainer(Containers sourceContainer, Contracts targetContract) {
        Containers clonedContainer = new Containers();
        clonedContainer.setContract(targetContract);
        clonedContainer.setContainerNo(sourceContainer.getContainerNo());
        clonedContainer.setName(sourceContainer.getName());
        clonedContainer.setContainerSize(sourceContainer.getContainerSize());
        clonedContainer.setContainerWeight(sourceContainer.getContainerWeight());
        clonedContainer.setComments(sourceContainer.getComments());
        
        // 克隆组件
        List<ContainerComponents> clonedComponents = new ArrayList<>();
        if (sourceContainer.getComponents() != null) {
            for (ContainerComponents sourceComponent : sourceContainer.getComponents()) {
                ContainerComponents clonedComponent = new ContainerComponents();
                clonedComponent.setContainer(clonedContainer);
                clonedComponent.setComponentNo(sourceComponent.getComponentNo());
                clonedComponent.setComponentName(sourceComponent.getComponentName());
                clonedComponent.setUnitCode(sourceComponent.getUnitCode());
                clonedComponent.setQuantity(sourceComponent.getQuantity());
                clonedComponent.setComments(sourceComponent.getComments());
                clonedComponents.add(clonedComponent);
            }
        }
        clonedContainer.setComponents(clonedComponents);
        
        return clonedContainer;
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
}

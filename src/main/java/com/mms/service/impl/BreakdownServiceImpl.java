package com.mms.service.impl;

import com.mms.entity.*;
import com.mms.repository.*;
import com.mms.service.BreakdownService;
import com.mms.service.ComponentCacheService;
import com.mms.service.ContainerComponentsBreakdownErpService;
import com.mms.utils.FastenerErpCodeFinder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.io.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.ColorConstants;

@Service
@RequiredArgsConstructor
@Slf4j
public class BreakdownServiceImpl implements BreakdownService {
    
    private final ContainersRepository containersRepository;
    private final ContainerComponentsRepository containerComponentsRepository;
    private final ComponentsRepository componentsRepository;
    private final ComponentsRelationshipRepository componentsRelationshipRepository;
    private final ContainerComponentsBreakdownRepository breakdownRepository;
    private final ContainerComponentsBreakdownProblemsRepository problemsRepository;
    private final ContractsRepository contractsRepository;
    private final ComponentCacheService componentCacheService;
    private final ContainerComponentsBreakdownErpService breakdownErpService;
    private final FastenerErpCodeFinder fastenerErpCodeFinder;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional
    public Map<String, Object> breakdownContainer(Long containerId) {
        log.info("开始对箱包进行工艺分解: containerId={}", containerId);
        
        Containers container = containersRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("箱包不存在"));
        
        // 清除该箱包之前的分解记录和问题部件记录
        breakdownRepository.deleteByContainerId(containerId);
        problemsRepository.deleteByContainerId(containerId);
        
        // 更新container状态为未分解
        container.setStatus(0);
        containersRepository.save(container);
        
        List<ContainerComponents> containerComponents = containerComponentsRepository.findByContainerId(containerId);
        List<Map<String, Object>> breakdownResults = new ArrayList<>();
        List<String> problemComponents = new ArrayList<>();
        
        for (ContainerComponents containerComponent : containerComponents) {
            Map<String, Object> result = processComponent(containerComponent);
            breakdownResults.add(result);
            
            // 收集问题部件并保存到问题件表
            @SuppressWarnings("unchecked")
            List<String> problems = (List<String>) result.get("problems");
            if (problems != null && !problems.isEmpty()) {
                problemComponents.addAll(problems);
                
                // 检查该部件是否在components表中存在
                Optional<Components> componentOpt = getComponentByCode(containerComponent.getComponentNo());
                if (componentOpt.isEmpty()) {
                    // 保存问题部件到问题件表
                    ContainerComponentsBreakdownProblems problem = new ContainerComponentsBreakdownProblems();
                    problem.setContainer(container);
                    problem.setComponentNo(containerComponent.getComponentNo());
                    problem.setName(containerComponent.getComponentName()); // 添加零部件名称
                    problem.setQuantity(containerComponent.getQuantity());
                    problem.setEntryTs(java.time.LocalDateTime.now());
                    problem.setEntryUser("SYS_USER");
                    problem.setLastUpdateTs(java.time.LocalDateTime.now());
                    problem.setLastUpdateUser("SYS_USER");
                    problemsRepository.save(problem);
                    
                    log.warn("保存问题部件到问题件表: containerId={}, componentNo={}, componentName={}, quantity={}", 
                        containerId, containerComponent.getComponentNo(), containerComponent.getComponentName(), containerComponent.getQuantity());
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("containerId", containerId);
        response.put("containerNo", container.getContainerNo());
        response.put("containerName", container.getName());
        response.put("breakdownResults", breakdownResults);
        response.put("problemComponents", problemComponents);
        response.put("totalComponents", containerComponents.size());
        response.put("processedComponents", breakdownResults.size());
        response.put("breakdownTime", new Date().toString());
        
        // 更新container状态为已分解
        container.setStatus(1);
        containersRepository.save(container);
        
        log.info("箱包工艺分解完成: containerId={}, 处理部件数={}, 问题部件数={}", 
            containerId, breakdownResults.size(), problemComponents.size());
        
        return response;
    }
    
    @Override
    @Transactional
    public Map<String, Object> breakdownContract(Long contractId) {
        log.info("开始对合同进行工艺分解: contractId={}", contractId);
        
        // 清除该合同的所有分解记录
        breakdownRepository.deleteByContractId(contractId);
        
        List<Containers> containers = containersRepository.findByContractId(contractId);
        List<Map<String, Object>> containerResults = new ArrayList<>();
        List<String> allProblemComponents = new ArrayList<>();
        int totalProcessedComponents = 0;
        
        for (Containers container : containers) {
            Map<String, Object> containerResult = breakdownContainer(container.getId());
            containerResults.add(containerResult);
            
            @SuppressWarnings("unchecked")
            List<String> problems = (List<String>) containerResult.get("problemComponents");
            if (problems != null) {
                allProblemComponents.addAll(problems);
            }
            
            totalProcessedComponents += (Integer) containerResult.get("processedComponents");
        }
        
        // 生成汇总表
        Map<String, Object> summary = generateBreakdownSummary(contractId);
        
        // 更新合同状态为已完成
        Contracts contract = contractsRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("合同不存在"));
        contract.setStatus(Contracts.ContractStatus.COMPLETED);
        contractsRepository.save(contract);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contractId", contractId);
        response.put("containerResults", containerResults);
        response.put("summary", summary);
        response.put("totalContainers", containers.size());
        response.put("totalProcessedComponents", totalProcessedComponents);
        response.put("allProblemComponents", allProblemComponents);
        response.put("breakdownTime", new Date().toString());
        
        log.info("合同工艺分解完成: contractId={}, 箱包数={}, 处理部件数={}, 合同状态已更新为已完成", 
            contractId, containers.size(), totalProcessedComponents);
        
        return response;
    }
    
    @Override
    public Map<String, Object> getContainerBreakdown(Long containerId) {
        Containers container = containersRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("箱包不存在"));
        
        List<ContainerComponentsBreakdown> breakdowns = breakdownRepository.findByContainerId(containerId);
        
        // 创建扁平的组件列表，包含所有组件（父组件和子组件）
        Map<String, Map<String, Object>> allComponents = new HashMap<>();
        List<String> problemComponents = new ArrayList<>();
        
        // 首先添加父组件（无论是否有分解结果都要显示）
        List<ContainerComponents> containerComponents = containerComponentsRepository.findByContainerId(containerId);
        for (ContainerComponents containerComponent : containerComponents) {
            String componentCode = containerComponent.getComponentNo();
            
            // 检查该组件是否在components表中存在
            Optional<Components> componentOpt = getComponentByCode(componentCode);
            
            if (!allComponents.containsKey(componentCode)) {
                Map<String, Object> componentInfo = new HashMap<>();
                componentInfo.put("componentCode", componentCode);
                componentInfo.put("name", containerComponent.getComponentName());
                componentInfo.put("quantity", containerComponent.getQuantity());
                
                if (componentOpt.isPresent()) {
                    // 如果找到匹配的组件，使用其属性
                    Components component = componentOpt.get();
                    componentInfo.put("procurementFlag", component.getProcurementFlag());
                    componentInfo.put("commonPartsFlag", component.getCommonPartsFlag());
                    componentInfo.put("remark", ""); // 正常组件无备注
                    componentInfo.put("erpCode", ""); // 父组件默认无ERP代码
                    
                    // 添加子组件信息
                    List<Map<String, Object>> childComponents = new ArrayList<>();
                    try {
                        // 使用Repository直接查询子组件关系，避免懒加载问题
                        List<ComponentsRelationship> relationships = componentsRelationshipRepository.findByParentId(component.getId());
                        if (relationships != null && !relationships.isEmpty()) {
                            for (ComponentsRelationship relationship : relationships) {
                                // 安全地获取子组件信息
                                if (relationship.getChild() != null) {
                                    Components child = relationship.getChild();
                                    Map<String, Object> childInfo = new HashMap<>();
                                    childInfo.put("componentCode", child.getComponentCode());
                                    childInfo.put("name", child.getName());
                                    childInfo.put("quantity", relationship.getQuantity());
                                    childComponents.add(childInfo);
                                } else {
                                    log.warn("组件关系 {} 的子组件为null", relationship.getId());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("获取组件 {} 的子组件时出错: {}", component.getComponentCode(), e.getMessage());
                    }
                    componentInfo.put("childComponents", childComponents);
                } else {
                    // 如果找不到匹配的组件，使用默认值并记录为问题部件
                    componentInfo.put("procurementFlag", false);
                    componentInfo.put("commonPartsFlag", false);
                    componentInfo.put("remark", "工件不存在"); // 问题组件备注
                    componentInfo.put("erpCode", ""); // 问题组件无ERP代码
                    componentInfo.put("childComponents", new ArrayList<>()); // 空子组件列表
                    String problem = String.format("部件编号 %s (%s) 在components表中找不到匹配项", 
                        componentCode, containerComponent.getComponentName());
                    problemComponents.add(problem);
                }
                
                componentInfo.put("isParentComponent", true); // 标记为父组件
                allComponents.put(componentCode, componentInfo);
            } else {
                // 合并同ComponentNo的父组件，累加数量
                Map<String, Object> existing = allComponents.get(componentCode);
                Integer currentQuantity = (Integer) existing.get("quantity");
                existing.put("quantity", currentQuantity + containerComponent.getQuantity());
            }
        }
        
        // 检查是否有分解结果
        if (breakdowns.isEmpty()) {
            // 即使没有分解结果，也要显示箱包中的工件
            Map<String, Object> response = new HashMap<>();
            response.put("containerId", containerId);
            response.put("containerNo", container.getContainerNo());
            response.put("containerName", container.getName());
            response.put("hasBreakdown", true); // 改为true，表示有分解表可显示
            response.put("allComponents", allComponents.values()); // 显示箱包中的工件
            response.put("problemComponents", problemComponents);
            response.put("totalComponents", allComponents.size());
            response.put("message", "该箱包尚未进行工艺分解，显示箱包中的工件");
            return response;
        }
        
        // 然后添加所有子组件
        for (ContainerComponentsBreakdown breakdown : breakdowns) {
            Components subComponent = breakdown.getSubComponent();
            String componentCode = subComponent.getComponentCode();
            
            // 获取ERP代码
            String erpCode = "";
            try {
                List<ContainerComponentsBreakdownErp> erpRecords = breakdownErpService.findByBreakdownId(breakdown.getId());
                if (!erpRecords.isEmpty()) {
                    erpCode = erpRecords.get(0).getErpCode();
                }
            } catch (Exception e) {
                log.debug("获取组件 {} 的ERP代码失败: {}", componentCode, e.getMessage());
            }
            
            if (allComponents.containsKey(componentCode)) {
                // 合并同ComponentNo的组件，累加数量
                Map<String, Object> existing = allComponents.get(componentCode);
                Integer currentQuantity = (Integer) existing.get("quantity");
                existing.put("quantity", currentQuantity + breakdown.getQuantity());
                // 更新其他信息
                existing.put("name", subComponent.getName());
                existing.put("procurementFlag", subComponent.getProcurementFlag());
                existing.put("commonPartsFlag", subComponent.getCommonPartsFlag());
                existing.put("remark", ""); // 子组件正常，无备注
                existing.put("isParentComponent", false); // 标记为子组件
                existing.put("erpCode", erpCode); // 添加ERP代码
            } else {
                // 新的子组件
                Map<String, Object> componentInfo = new HashMap<>();
                componentInfo.put("componentCode", componentCode);
                componentInfo.put("name", subComponent.getName());
                componentInfo.put("quantity", breakdown.getQuantity());
                componentInfo.put("procurementFlag", subComponent.getProcurementFlag());
                componentInfo.put("commonPartsFlag", subComponent.getCommonPartsFlag());
                componentInfo.put("remark", ""); // 子组件正常，无备注
                componentInfo.put("isParentComponent", false); // 标记为子组件
                componentInfo.put("erpCode", erpCode); // 添加ERP代码
                allComponents.put(componentCode, componentInfo);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("containerId", containerId);
        response.put("containerNo", container.getContainerNo());
        response.put("containerName", container.getName());
        response.put("hasBreakdown", true);
        response.put("allComponents", allComponents.values()); // 返回扁平的组件列表
        response.put("problemComponents", problemComponents);
        response.put("totalComponents", allComponents.size());
        
        return response;
    }
    
    @Override
    @Transactional
    public Map<String, Object> deleteContainerBreakdown(Long containerId) {
        log.info("删除箱包分解结果: containerId={}", containerId);
        
        Containers container = containersRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("箱包不存在"));
        
        // 删除该箱包的所有分解记录
        int deletedCount = breakdownRepository.deleteByContainerId(containerId);
        
        // 更新container状态为未分解
        container.setStatus(0);
        containersRepository.save(container);
        
        Map<String, Object> response = new HashMap<>();
        response.put("containerId", containerId);
        response.put("containerNo", container.getContainerNo());
        response.put("containerName", container.getName());
        response.put("deletedCount", deletedCount);
        response.put("message", "分解结果已删除");
        
        log.info("箱包分解结果删除完成: containerId={}, 删除记录数={}", containerId, deletedCount);
        
        return response;
    }
    
    @Override
    public Map<String, Object> getContractBreakdownSummary(Long contractId) {
        return generateBreakdownSummary(contractId);
    }
    
    @Override
    public byte[] exportBreakdown(Long contractId, String format) {
        // TODO: 实现导出功能
        log.info("导出工艺分解表: contractId={}, format={}", contractId, format);
        return new byte[0];
    }
    
    /**
     * 从缓存或数据库中获取零部件信息
     */
    private Optional<Components> getComponentByCode(String componentCode) {
        // 优先从Redis缓存中获取
        Optional<String> cachedComponentJson = componentCacheService.getComponentFromCache(componentCode);
        
        if (cachedComponentJson.isPresent()) {
            try {
                Components component = objectMapper.readValue(cachedComponentJson.get(), Components.class);
                log.debug("从缓存获取零部件: {}", componentCode);
                return Optional.of(component);
            } catch (Exception e) {
                log.error("解析缓存中的零部件JSON失败: componentCode={}, error={}", 
                    componentCode, e.getMessage());
            }
        }
        
        // 缓存中没有或解析失败，从数据库获取
        Optional<Components> componentOpt = componentsRepository.findByComponentCode(componentCode);
        if (componentOpt.isPresent()) {
            log.debug("从数据库获取零部件: {}", componentCode);
            
            // 将获取到的零部件存储到缓存中
            try {
                String componentJson = objectMapper.writeValueAsString(componentOpt.get());
                componentCacheService.putComponentToCache(componentCode, componentJson);
            } catch (Exception e) {
                log.error("将零部件存储到缓存失败: componentCode={}, error={}", 
                    componentCode, e.getMessage());
            }
        }
        
        return componentOpt;
    }
    
    /**
     * 处理单个部件
     */
    private Map<String, Object> processComponent(ContainerComponents containerComponent) {
        Map<String, Object> result = new HashMap<>();
        List<String> problems = new ArrayList<>();
        
        result.put("componentNo", containerComponent.getComponentNo());
        result.put("componentName", containerComponent.getComponentName());
        result.put("quantity", containerComponent.getQuantity());
        
        // 根据component_no在缓存或数据库中查找匹配的部件
        Optional<Components> componentOpt = getComponentByCode(containerComponent.getComponentNo());
        
        if (componentOpt.isPresent()) {
            Components component = componentOpt.get();
            // 不直接序列化实体对象，只保存需要的字段
            result.put("procurementFlag", component.getProcurementFlag());
            result.put("commonPartsFlag", component.getCommonPartsFlag());
            
            // 首先保存当前组件本身到分解表（即使没有子组件）
            saveBreakdownRecord(containerComponent, component, containerComponent.getQuantity());
            
            // 递归查找所有子部件，并保存到数据库
            Set<String> processedComponents = new HashSet<>(); // 防止循环引用
            processChildComponentsRecursively(component, containerComponent, processedComponents);
            
            // 不再返回subComponents，因为所有组件都会在总的列表中显示
            result.put("subComponents", new ArrayList<>());
        } else {
            // 找不到匹配的部件，记录为问题部件
            String problem = String.format("部件编号 %s (%s) 在components表中找不到匹配项", 
                containerComponent.getComponentNo(), containerComponent.getComponentName());
            problems.add(problem);
        }
        
        result.put("problems", problems);
        return result;
    }
    
    /**
     * 递归处理子部件
     */
    private void processChildComponentsRecursively(Components parentComponent, 
                                                   ContainerComponents containerComponent, 
                                                   Set<String> processedComponents) {
        // 防止循环引用
        String componentKey = parentComponent.getId().toString();
        if (processedComponents.contains(componentKey)) {
            return;
        }
        processedComponents.add(componentKey);
        
        // 查找直接子部件
        List<ComponentsRelationship> childRelations = componentsRelationshipRepository.findByParentId(parentComponent.getId());
        
        for (ComponentsRelationship relation : childRelations) {
            Components childComponent = relation.getChild();
            
            // 计算子组件数量：父组件数量 × 子组件配置表的数量
            Integer childQuantity = containerComponent.getQuantity() * relation.getQuantity();
            
            // 保存分解记录
            saveBreakdownRecord(containerComponent, childComponent, childQuantity);
            
            // 递归处理子部件的子部件
            processChildComponentsRecursively(childComponent, containerComponent, processedComponents);
        }
    }
    
    /**
     * 保存分解记录
     */
    private void saveBreakdownRecord(ContainerComponents containerComponent, Components subComponent, Integer quantity) {
        ContainerComponentsBreakdown breakdown = new ContainerComponentsBreakdown();
        breakdown.setContainerComponent(containerComponent);
        breakdown.setSubComponent(subComponent);
        breakdown.setContainer(containerComponent.getContainer());
        breakdown.setQuantity(quantity);
        
        ContainerComponentsBreakdown savedBreakdown = breakdownRepository.save(breakdown);
        
        // 调用FastenerErpCodeFinder查找ERP代码
        try {
            FastenerErpCodeFinder.ErpCodeResult erpResult = fastenerErpCodeFinder.findErpCode(
                    subComponent.getId(), 
                    subComponent.getComponentCode(), 
                    subComponent.getName()
            );
            
            // 如果isFastenerComponent == false，直接跳过
            if (!erpResult.isFastenerComponent()) {
                log.debug("组件不是紧固件，跳过ERP代码查找: componentId={}, componentCode={}", 
                        subComponent.getId(), subComponent.getComponentCode());
                return;
            }
            
            // 创建ERP代码记录
            ContainerComponentsBreakdownErp erpRecord = new ContainerComponentsBreakdownErp();
            erpRecord.setBreakdown(savedBreakdown);
            
            if (erpResult.isSuccess()) {
                // 成功匹配，写入ERP代码
                erpRecord.setErpCode(erpResult.getErpCode());
                erpRecord.setComments(String.format("自动生成 - 匹配产品代码: %s, 规格: %s, 等级: %s, 表面处理: %s",
                        erpResult.getMatchedProductCode(),
                        erpResult.getMatchedSpecs(),
                        erpResult.getMatchedLevel(),
                        erpResult.getMatchedSurfaceTreatment()));
                
                log.info("成功生成ERP代码记录: breakdownId={}, componentId={}, erpCode={}", 
                        savedBreakdown.getId(), subComponent.getId(), erpResult.getErpCode());
            } else {
                // 查找失败，标记为未知物料
                erpRecord.setErpCode(null);
                erpRecord.setComments("未知物料: " + erpResult.getErrorMessage());
                
                log.warn("ERP代码查找失败: breakdownId={}, componentId={}, error={}", 
                        savedBreakdown.getId(), subComponent.getId(), erpResult.getErrorMessage());
            }
            
            // 保存ERP代码记录
            breakdownErpService.create(erpRecord);
            
        } catch (Exception e) {
            log.error("ERP代码查找过程中发生异常: breakdownId={}, componentId={}, error={}", 
                    savedBreakdown.getId(), subComponent.getId(), e.getMessage(), e);
            
            // 即使发生异常，也创建一个记录用于标记
            ContainerComponentsBreakdownErp erpRecord = new ContainerComponentsBreakdownErp();
            erpRecord.setBreakdown(savedBreakdown);
            erpRecord.setErpCode(null);
            erpRecord.setComments("ERP代码查找异常: " + e.getMessage());
            
            try {
                breakdownErpService.create(erpRecord);
            } catch (Exception ex) {
                log.error("保存ERP代码异常记录失败: breakdownId={}, error={}", 
                        savedBreakdown.getId(), ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * 生成分解汇总表
     */
    private Map<String, Object> generateBreakdownSummary(Long contractId) {
        List<ContainerComponentsBreakdown> allBreakdowns = breakdownRepository.findByContractId(contractId);
        
        // 创建扁平的组件汇总，包含所有组件（父组件和子组件）
        Map<String, Map<String, Object>> allComponentsSummary = new HashMap<>();
        
        // 首先添加所有父组件
        List<Containers> containers = containersRepository.findByContractId(contractId);
        for (Containers container : containers) {
            List<ContainerComponents> containerComponents = containerComponentsRepository.findByContainerId(container.getId());
            for (ContainerComponents containerComponent : containerComponents) {
                String componentCode = containerComponent.getComponentNo();
                if (allComponentsSummary.containsKey(componentCode)) {
                    // 合并同ComponentNo的父组件，累加数量
                    Map<String, Object> existing = allComponentsSummary.get(componentCode);
                    Integer currentQuantity = (Integer) existing.get("totalQuantity");
                    existing.put("totalQuantity", currentQuantity + containerComponent.getQuantity());
                } else {
                    // 新的父组件
                    Map<String, Object> componentInfo = new HashMap<>();
                    componentInfo.put("componentCode", componentCode);
                    componentInfo.put("name", containerComponent.getComponentName());
                    componentInfo.put("containerName", container.getName()); // 添加箱包名称
                    componentInfo.put("procurementFlag", false); // 默认值
                    componentInfo.put("commonPartsFlag", false); // 默认值
                    componentInfo.put("totalQuantity", containerComponent.getQuantity());
                    componentInfo.put("erpCode", ""); // 父组件默认无ERP代码
                    componentInfo.put("remark", ""); // 默认无备注
                    componentInfo.put("isParentComponent", true); // 标记为父组件
                    allComponentsSummary.put(componentCode, componentInfo);
                }
            }
        }
        
        // 然后添加所有子组件
        for (ContainerComponentsBreakdown breakdown : allBreakdowns) {
            Components component = breakdown.getSubComponent();
            String componentCode = component.getComponentCode();
            
            // 获取ERP代码
            String erpCode = "";
            try {
                List<ContainerComponentsBreakdownErp> erpRecords = breakdownErpService.findByBreakdownId(breakdown.getId());
                if (!erpRecords.isEmpty()) {
                    erpCode = erpRecords.get(0).getErpCode() != null ? erpRecords.get(0).getErpCode() : "";
                }
            } catch (Exception e) {
                log.debug("获取组件 {} 的ERP代码失败: {}", componentCode, e.getMessage());
            }
            
            // 获取箱包名称
            String containerName = breakdown.getContainer() != null ? breakdown.getContainer().getName() : "未知箱包";
            
            if (allComponentsSummary.containsKey(componentCode)) {
                // 合并同ComponentNo的组件，累加数量
                Map<String, Object> existing = allComponentsSummary.get(componentCode);
                Integer currentQuantity = (Integer) existing.get("totalQuantity");
                existing.put("totalQuantity", currentQuantity + breakdown.getQuantity());
                // 更新其他信息
                existing.put("name", component.getName());
                existing.put("containerName", containerName); // 更新箱包名称
                existing.put("procurementFlag", component.getProcurementFlag());
                existing.put("commonPartsFlag", component.getCommonPartsFlag());
                existing.put("erpCode", erpCode); // 更新ERP代码
                existing.put("remark", ""); // 子组件正常，无备注
                existing.put("isParentComponent", false); // 标记为子组件
            } else {
                // 新的子组件
                Map<String, Object> componentInfo = new HashMap<>();
                componentInfo.put("componentCode", componentCode);
                componentInfo.put("name", component.getName());
                componentInfo.put("containerName", containerName); // 添加箱包名称
                componentInfo.put("procurementFlag", component.getProcurementFlag());
                componentInfo.put("commonPartsFlag", component.getCommonPartsFlag());
                componentInfo.put("totalQuantity", breakdown.getQuantity());
                componentInfo.put("erpCode", erpCode); // 添加ERP代码
                componentInfo.put("remark", ""); // 子组件正常，无备注
                componentInfo.put("isParentComponent", false); // 标记为子组件
                allComponentsSummary.put(componentCode, componentInfo);
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("contractId", contractId);
        summary.put("allComponents", allComponentsSummary.values()); // 返回扁平的组件列表
        summary.put("totalComponents", allComponentsSummary.size());
        
        return summary;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> mergeBreakdownTables(List<Integer> containerIds) {
        log.info("开始合并分解表: containerIds={}", containerIds);
        
        if (containerIds == null || containerIds.isEmpty()) {
            throw new RuntimeException("箱包ID列表不能为空");
        }
        
        // 验证所有箱包都已分解
        List<Long> longContainerIds = containerIds.stream().map(Integer::longValue).collect(java.util.stream.Collectors.toList());
        List<Containers> containers = containersRepository.findAllById(longContainerIds);
        for (Containers container : containers) {
            if (container.getStatus() != 1) {
                throw new RuntimeException(String.format("箱包 %s 尚未分解，无法合并", container.getContainerNo()));
            }
        }
        
        // 直接从数据库读取分解数据，不进行任何写入操作
        Map<String, Map<String, Object>> mergedComponents = new HashMap<>();
        Map<String, Integer> mergedProblems = new HashMap<>();
        int totalContainers = 0;
        
        // 获取合同ID
        Long contractId = containers.get(0).getContract().getId();
        
        // 直接从container_components_breakdown表读取分解数据
        List<ContainerComponentsBreakdown> allBreakdowns = breakdownRepository.findByContractId(contractId);
        
        // 按箱包ID过滤分解数据
        List<ContainerComponentsBreakdown> filteredBreakdowns = allBreakdowns.stream()
            .filter(breakdown -> longContainerIds.contains(breakdown.getContainer().getId()))
            .collect(java.util.stream.Collectors.toList());
        
        // 合并正常部件数据
        for (ContainerComponentsBreakdown breakdown : filteredBreakdowns) {
            Components subComponent = breakdown.getSubComponent();
            if (subComponent != null) {
                String componentCode = subComponent.getComponentCode();
                Integer quantity = breakdown.getQuantity();
                
                if (mergedComponents.containsKey(componentCode)) {
                    // 累加数量
                    Map<String, Object> existing = mergedComponents.get(componentCode);
                    Integer currentQuantity = (Integer) existing.get("quantity");
                    existing.put("quantity", currentQuantity + quantity);
                } else {
                    // 新组件
                    Map<String, Object> newComponent = new HashMap<>();
                    newComponent.put("componentCode", componentCode);
                    newComponent.put("name", subComponent.getName());
                    newComponent.put("quantity", quantity);
                        newComponent.put("erpCode", ""); // ERP代码需要从ContainerComponentsBreakdownErp表获取
                    newComponent.put("procurementFlag", subComponent.getProcurementFlag());
                    newComponent.put("commonPartsFlag", subComponent.getCommonPartsFlag());
                    newComponent.put("remark", null); // 正常部件没有备注
                    mergedComponents.put(componentCode, newComponent);
                }
            }
        }
        
        // 直接从container_components_breakdown_problems表读取问题部件数据
        for (Long containerId : longContainerIds) {
            List<ContainerComponentsBreakdownProblems> containerProblems = problemsRepository.findByContainerId(containerId);
            for (ContainerComponentsBreakdownProblems problem : containerProblems) {
                String componentNo = problem.getComponentNo();
                Integer quantity = problem.getQuantity();
                
                if (mergedProblems.containsKey(componentNo)) {
                    mergedProblems.put(componentNo, mergedProblems.get(componentNo) + quantity);
                } else {
                    mergedProblems.put(componentNo, quantity);
                }
            }
        }
        
        // 将问题部件添加到合并结果中
        for (Map.Entry<String, Integer> entry : mergedProblems.entrySet()) {
            String componentNo = entry.getKey();
            Integer quantity = entry.getValue();
            
            Map<String, Object> problemComponent = new HashMap<>();
            problemComponent.put("componentCode", componentNo);
            problemComponent.put("name", componentNo); // 问题部件没有名称信息
            problemComponent.put("quantity", quantity);
            problemComponent.put("erpCode", null);
            problemComponent.put("procurementFlag", false);
            problemComponent.put("commonPartsFlag", false);
            problemComponent.put("remark", "在components表中找不到匹配项");
            
            mergedComponents.put(componentNo, problemComponent);
        }
        
        totalContainers = containers.size();
        
        // 生成PDF下载链接（不保存到数据库）
        String downloadUrl = generateMergedBreakdownPdfUrl(contractId, mergedComponents);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "合并分解表成功");
        result.put("totalContainers", totalContainers);
        result.put("totalComponents", mergedComponents.size());
        result.put("totalProblems", mergedProblems.size());
        result.put("downloadUrl", downloadUrl);
        result.put("mergedComponents", mergedComponents.values()); // 返回合并后的数据
        result.put("mergedProblems", mergedProblems); // 返回问题部件数据
        
        log.info("合并分解表完成: contractId={}, totalContainers={}, totalComponents={}, totalProblems={}", 
            contractId, totalContainers, mergedComponents.size(), mergedProblems.size());
        
        return result;
    }
    
    /**
     * 生成合并分解表PDF下载链接
     */
    private String generateMergedBreakdownPdfUrl(Long contractId, Map<String, Map<String, Object>> mergedComponents) {
        try {
            // 获取合同号以生成包含文件名的URL
            String contractNo = getContractNoById(contractId);
            String fileName = String.format("%s_合并分解表.pdf", contractNo);
            // URL编码文件名以处理特殊字符
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            
            // 返回相对路径，让前端处理端口转换
            return "/api/breakdown/merged/" + contractId + "/download/" + encodedFileName;
        } catch (Exception e) {
            log.error("生成PDF下载链接失败: contractId={}, error={}", contractId, e.getMessage(), e);
            // 降级处理：返回不带文件名的URL
            return "/api/breakdown/merged/" + contractId + "/download";
        }
    }
    
    @Override
    public byte[] generateMergedBreakdownPdf(Long contractId) {
        log.info("生成合并分解表PDF: contractId={}", contractId);
        
        try {
            // 直接从分解表读取数据，不依赖合并表
            List<ContainerComponentsBreakdown> allBreakdowns = breakdownRepository.findByContractId(contractId);
            List<ContainerComponentsBreakdownProblems> allProblems = problemsRepository.findByContractId(contractId);
            
            if (allBreakdowns.isEmpty() && allProblems.isEmpty()) {
                throw new RuntimeException("没有找到分解数据");
            }
            
            // 获取合同信息
            Optional<Contracts> contractOpt = contractsRepository.findById(contractId);
            if (contractOpt.isEmpty()) {
                throw new RuntimeException("合同不存在");
            }
            Contracts contract = contractOpt.get();
            
            // 合并分解数据，按部件编号合并
            Map<String, Map<String, Object>> mergedComponents = new HashMap<>();
            
            // 处理正常部件
            for (ContainerComponentsBreakdown breakdown : allBreakdowns) {
                Components subComponent = breakdown.getSubComponent();
                if (subComponent != null) {
                    String componentCode = subComponent.getComponentCode();
                    Integer quantity = breakdown.getQuantity();
                    
                    // 获取ERP代码
                    String erpCode = "";
                    try {
                        List<ContainerComponentsBreakdownErp> erpRecords = breakdownErpService.findByBreakdownId(breakdown.getId());
                        if (!erpRecords.isEmpty()) {
                            erpCode = erpRecords.get(0).getErpCode() != null ? erpRecords.get(0).getErpCode() : "";
                        }
                    } catch (Exception e) {
                        log.debug("获取组件 {} 的ERP代码失败: {}", componentCode, e.getMessage());
                    }
                    
                    if (mergedComponents.containsKey(componentCode)) {
                        // 累加数量
                        Map<String, Object> existing = mergedComponents.get(componentCode);
                        Integer currentQuantity = (Integer) existing.get("quantity");
                        existing.put("quantity", currentQuantity + quantity);
                    } else {
                        // 新组件
                        Map<String, Object> component = new HashMap<>();
                        component.put("type", "normal");
                        component.put("componentCode", componentCode);
                        component.put("name", subComponent.getName());
                        component.put("quantity", quantity);
                        component.put("erpCode", erpCode);
                        component.put("procurementFlag", subComponent.getProcurementFlag());
                        component.put("commonPartsFlag", subComponent.getCommonPartsFlag());
                        component.put("remark", "");
                        mergedComponents.put(componentCode, component);
                    }
                }
            }
            
            // 处理问题部件
            for (ContainerComponentsBreakdownProblems problem : allProblems) {
                String componentNo = problem.getComponentNo();
                Integer quantity = problem.getQuantity();
                
                if (mergedComponents.containsKey(componentNo)) {
                    // 累加数量
                    Map<String, Object> existing = mergedComponents.get(componentNo);
                    Integer currentQuantity = (Integer) existing.get("quantity");
                    existing.put("quantity", currentQuantity + quantity);
                    // 更新备注为问题部件
                    existing.put("remark", "工件不存在");
                } else {
                    // 新问题组件
                    Map<String, Object> problemComponent = new HashMap<>();
                    problemComponent.put("type", "problem");
                    problemComponent.put("componentCode", componentNo);
                    problemComponent.put("name", problem.getName() != null ? problem.getName() : componentNo);
                    problemComponent.put("quantity", quantity);
                    problemComponent.put("erpCode", "");
                    problemComponent.put("procurementFlag", false);
                    problemComponent.put("commonPartsFlag", false);
                    problemComponent.put("remark", "工件不存在");
                    
                    mergedComponents.put(componentNo, problemComponent);
                }
            }
            
            // 转换为列表用于排序和显示
            List<Map<String, Object>> allRows = new ArrayList<>(mergedComponents.values());
            
            // 按部件编号排序
            allRows.sort((row1, row2) -> {
                String componentCode1 = (String) row1.get("componentCode");
                String componentCode2 = (String) row2.get("componentCode");
                if (componentCode1 == null && componentCode2 == null) return 0;
                if (componentCode1 == null) return 1;
                if (componentCode2 == null) return -1;
                return componentCode1.compareTo(componentCode2);
            });
            
            // 创建PDF文档
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            
            // 设置页面为横向
            PageSize pageSize = PageSize.A4.rotate();
            Document document = new Document(pdfDoc, pageSize);
            
            // 设置中文字体支持
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            PdfFont boldFont = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            
            // 添加标题
            Paragraph title = new Paragraph("合并分解表")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(title);
            
            // 添加合同信息
            Paragraph contractInfo = new Paragraph()
                .setFont(font)
                .setFontSize(12)
                .add("合同号: " + contract.getContractNo() + "\n")
                .add("项目名称: " + contract.getProjectName() + "\n")
                .add("生成时间: " + java.time.LocalDateTime.now().toString() + "\n")
                .setMarginBottom(20);
            document.add(contractInfo);
            
            // 创建表格
            Table table = new Table(8).useAllAvailableWidth();
            table.setFont(font).setFontSize(10);
            
            // 添加表头
            table.addHeaderCell(new Cell().add(new Paragraph("序号").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("部件编号").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("ERP代码").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("部件名称").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("数量").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("是否外购").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("是否通用件").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("备注").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            
            // 添加数据行
            int rowNumber = 1;
            for (Map<String, Object> component : allRows) {
                boolean isProblemRow = "problem".equals(component.get("type"));
                
                // 序号列
                Cell indexCell = new Cell().add(new Paragraph(String.valueOf(rowNumber))).setTextAlignment(TextAlignment.CENTER);
                if (isProblemRow) {
                    indexCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(indexCell);
                
                // 部件编号列
                String componentCode = (String) component.get("componentCode");
                Cell codeCell = new Cell().add(new Paragraph(componentCode != null ? componentCode : ""));
                if (isProblemRow) {
                    codeCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(codeCell);
                
                // ERP代码列
                String erpCode = (String) component.get("erpCode");
                Cell erpCodeCell = new Cell().add(new Paragraph(erpCode != null ? erpCode : ""));
                if (isProblemRow) {
                    erpCodeCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(erpCodeCell);
                
                // 部件名称列
                String componentName = (String) component.get("name");
                Cell nameCell = new Cell().add(new Paragraph(componentName != null ? componentName : ""));
                if (isProblemRow) {
                    nameCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(nameCell);
                
                // 数量列
                Object quantityObj = component.get("quantity");
                String quantityStr = quantityObj != null ? String.valueOf(quantityObj) : "0";
                Cell quantityCell = new Cell().add(new Paragraph(quantityStr)).setTextAlignment(TextAlignment.CENTER);
                if (isProblemRow) {
                    quantityCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(quantityCell);
                
                // 是否外购列
                Boolean procurementFlag = (Boolean) component.get("procurementFlag");
                Cell procurementCell = new Cell().add(new Paragraph(procurementFlag != null && procurementFlag ? "是" : "否")).setTextAlignment(TextAlignment.CENTER);
                if (isProblemRow) {
                    procurementCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(procurementCell);
                
                // 是否通用件列
                Boolean commonPartsFlag = (Boolean) component.get("commonPartsFlag");
                Cell commonPartsCell = new Cell().add(new Paragraph(commonPartsFlag != null && commonPartsFlag ? "是" : "否")).setTextAlignment(TextAlignment.CENTER);
                if (isProblemRow) {
                    commonPartsCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(commonPartsCell);
                
                // 备注列
                String remark = (String) component.get("remark");
                Cell remarkCell = new Cell().add(new Paragraph(remark != null ? remark : "")).setTextAlignment(TextAlignment.CENTER);
                if (isProblemRow) {
                    remarkCell.setBackgroundColor(ColorConstants.RED)
                            .setFontColor(ColorConstants.WHITE)
                            .setFont(boldFont);
                }
                table.addCell(remarkCell);
                
                rowNumber++;
            }
            
            document.add(table);
            document.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            log.info("合并分解表PDF生成完成: contractId={}, size={} bytes", contractId, pdfBytes.length);
            
            return pdfBytes;
            
        } catch (Exception e) {
            log.error("生成合并分解表PDF失败: contractId={}, error={}", contractId, e.getMessage(), e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }
    
    @Override
    public String getContractNoById(Long contractId) {
        try {
            Optional<Contracts> contractOpt = contractsRepository.findById(contractId);
            if (contractOpt.isEmpty()) {
                throw new RuntimeException("合同不存在: contractId=" + contractId);
            }
            return contractOpt.get().getContractNo();
        } catch (Exception e) {
            log.error("获取合同号失败: contractId={}, error={}", contractId, e.getMessage(), e);
            throw new RuntimeException("获取合同号失败: " + e.getMessage(), e);
        }
    }
}

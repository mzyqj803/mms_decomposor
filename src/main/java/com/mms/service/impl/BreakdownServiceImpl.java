package com.mms.service.impl;

import com.mms.entity.*;
import com.mms.repository.*;
import com.mms.service.BreakdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BreakdownServiceImpl implements BreakdownService {
    
    private final ContainersRepository containersRepository;
    private final ContainerComponentsRepository containerComponentsRepository;
    private final ComponentsRepository componentsRepository;
    private final ComponentsRelationshipRepository componentsRelationshipRepository;
    private final ContainerComponentsBreakdownRepository breakdownRepository;
    
    @Override
    @Transactional
    public Map<String, Object> breakdownContainer(Long containerId) {
        log.info("开始对箱包进行工艺分解: containerId={}", containerId);
        
        Containers container = containersRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("箱包不存在"));
        
        // 清除该箱包之前的分解记录
        breakdownRepository.deleteByContainerId(containerId);
        
        // 更新container状态为未分解
        container.setStatus(0);
        containersRepository.save(container);
        
        List<ContainerComponents> containerComponents = containerComponentsRepository.findByContainerId(containerId);
        List<Map<String, Object>> breakdownResults = new ArrayList<>();
        List<String> problemComponents = new ArrayList<>();
        
        for (ContainerComponents containerComponent : containerComponents) {
            Map<String, Object> result = processComponent(containerComponent);
            breakdownResults.add(result);
            
            // 收集问题部件
            @SuppressWarnings("unchecked")
            List<String> problems = (List<String>) result.get("problems");
            if (problems != null && !problems.isEmpty()) {
                problemComponents.addAll(problems);
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("contractId", contractId);
        response.put("containerResults", containerResults);
        response.put("summary", summary);
        response.put("totalContainers", containers.size());
        response.put("totalProcessedComponents", totalProcessedComponents);
        response.put("allProblemComponents", allProblemComponents);
        response.put("breakdownTime", new Date().toString());
        
        log.info("合同工艺分解完成: contractId={}, 箱包数={}, 处理部件数={}", 
            contractId, containers.size(), totalProcessedComponents);
        
        return response;
    }
    
    @Override
    public Map<String, Object> getContainerBreakdown(Long containerId) {
        Containers container = containersRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("箱包不存在"));
        
        List<ContainerComponentsBreakdown> breakdowns = breakdownRepository.findByContainerId(containerId);
        
        // 检查是否有分解结果
        if (breakdowns.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("containerId", containerId);
            response.put("containerNo", container.getContainerNo());
            response.put("containerName", container.getName());
            response.put("hasBreakdown", false);
            response.put("message", "该箱包尚未进行工艺分解");
            return response;
        }
        
        // 创建扁平的组件列表，包含所有组件（父组件和子组件）
        Map<String, Map<String, Object>> allComponents = new HashMap<>();
        List<String> problemComponents = new ArrayList<>();
        
        // 首先添加父组件
        List<ContainerComponents> containerComponents = containerComponentsRepository.findByContainerId(containerId);
        for (ContainerComponents containerComponent : containerComponents) {
            String componentCode = containerComponent.getComponentNo();
            
            // 检查该组件是否在components表中存在
            Optional<Components> componentOpt = componentsRepository.findByComponentCode(componentCode);
            
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
                } else {
                    // 如果找不到匹配的组件，使用默认值并记录为问题部件
                    componentInfo.put("procurementFlag", false);
                    componentInfo.put("commonPartsFlag", false);
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
        
        // 然后添加所有子组件
        for (ContainerComponentsBreakdown breakdown : breakdowns) {
            Components subComponent = breakdown.getSubComponent();
            String componentCode = subComponent.getComponentCode();
            
            if (allComponents.containsKey(componentCode)) {
                // 合并同ComponentNo的组件，累加数量
                Map<String, Object> existing = allComponents.get(componentCode);
                Integer currentQuantity = (Integer) existing.get("quantity");
                existing.put("quantity", currentQuantity + breakdown.getQuantity());
                // 更新其他信息
                existing.put("name", subComponent.getName());
                existing.put("procurementFlag", subComponent.getProcurementFlag());
                existing.put("commonPartsFlag", subComponent.getCommonPartsFlag());
                existing.put("isParentComponent", false); // 标记为子组件
            } else {
                // 新的子组件
                Map<String, Object> componentInfo = new HashMap<>();
                componentInfo.put("componentCode", componentCode);
                componentInfo.put("name", subComponent.getName());
                componentInfo.put("quantity", breakdown.getQuantity());
                componentInfo.put("procurementFlag", subComponent.getProcurementFlag());
                componentInfo.put("commonPartsFlag", subComponent.getCommonPartsFlag());
                componentInfo.put("isParentComponent", false); // 标记为子组件
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
     * 处理单个部件
     */
    private Map<String, Object> processComponent(ContainerComponents containerComponent) {
        Map<String, Object> result = new HashMap<>();
        List<String> problems = new ArrayList<>();
        
        result.put("componentNo", containerComponent.getComponentNo());
        result.put("componentName", containerComponent.getComponentName());
        result.put("quantity", containerComponent.getQuantity());
        
        // 根据component_no在components表中查找匹配的部件
        Optional<Components> componentOpt = componentsRepository.findByComponentCode(containerComponent.getComponentNo());
        
        if (componentOpt.isPresent()) {
            Components component = componentOpt.get();
            // 不直接序列化实体对象，只保存需要的字段
            result.put("procurementFlag", component.getProcurementFlag());
            result.put("commonPartsFlag", component.getCommonPartsFlag());
            
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
            
            // 保存分解记录
            saveBreakdownRecord(containerComponent, childComponent, containerComponent.getQuantity());
            
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
        
        breakdownRepository.save(breakdown);
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
                    componentInfo.put("procurementFlag", false); // 默认值
                    componentInfo.put("commonPartsFlag", false); // 默认值
                    componentInfo.put("totalQuantity", containerComponent.getQuantity());
                    componentInfo.put("isParentComponent", true); // 标记为父组件
                    allComponentsSummary.put(componentCode, componentInfo);
                }
            }
        }
        
        // 然后添加所有子组件
        for (ContainerComponentsBreakdown breakdown : allBreakdowns) {
            Components component = breakdown.getSubComponent();
            String componentCode = component.getComponentCode();
            
            if (allComponentsSummary.containsKey(componentCode)) {
                // 合并同ComponentNo的组件，累加数量
                Map<String, Object> existing = allComponentsSummary.get(componentCode);
                Integer currentQuantity = (Integer) existing.get("totalQuantity");
                existing.put("totalQuantity", currentQuantity + breakdown.getQuantity());
                // 更新其他信息
                existing.put("name", component.getName());
                existing.put("procurementFlag", component.getProcurementFlag());
                existing.put("commonPartsFlag", component.getCommonPartsFlag());
                existing.put("isParentComponent", false); // 标记为子组件
            } else {
                // 新的子组件
                Map<String, Object> componentInfo = new HashMap<>();
                componentInfo.put("componentCode", componentCode);
                componentInfo.put("name", component.getName());
                componentInfo.put("procurementFlag", component.getProcurementFlag());
                componentInfo.put("commonPartsFlag", component.getCommonPartsFlag());
                componentInfo.put("totalQuantity", breakdown.getQuantity());
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
}

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
        
        // 清除之前的分解记录
        breakdownRepository.deleteByContractId(container.getContract().getId());
        
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
        
        log.info("箱包工艺分解完成: containerId={}, 处理部件数={}, 问题部件数={}", 
            containerId, breakdownResults.size(), problemComponents.size());
        
        return response;
    }
    
    @Override
    @Transactional
    public Map<String, Object> breakdownContract(Long contractId) {
        log.info("开始对合同进行工艺分解: contractId={}", contractId);
        
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
        
        log.info("合同工艺分解完成: contractId={}, 箱包数={}, 处理部件数={}", 
            contractId, containers.size(), totalProcessedComponents);
        
        return response;
    }
    
    @Override
    public Map<String, Object> getContainerBreakdown(Long containerId) {
        Containers container = containersRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("箱包不存在"));
        
        List<ContainerComponentsBreakdown> breakdowns = breakdownRepository.findByContainerId(containerId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("containerId", containerId);
        response.put("containerNo", container.getContainerNo());
        response.put("containerName", container.getName());
        response.put("breakdowns", breakdowns);
        
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
            result.put("component", component);
            result.put("procurementFlag", component.getProcurementFlag());
            result.put("commonPartsFlag", component.getCommonPartsFlag());
            
            // 查找子部件
            List<ComponentsRelationship> childRelations = componentsRelationshipRepository.findByParentId(component.getId());
            List<Map<String, Object>> subComponents = new ArrayList<>();
            
            for (ComponentsRelationship relation : childRelations) {
                Components subComponent = relation.getChild();
                Map<String, Object> subComponentInfo = new HashMap<>();
                subComponentInfo.put("componentCode", subComponent.getComponentCode());
                subComponentInfo.put("name", subComponent.getName());
                subComponentInfo.put("procurementFlag", subComponent.getProcurementFlag());
                subComponentInfo.put("commonPartsFlag", subComponent.getCommonPartsFlag());
                subComponents.add(subComponentInfo);
                
                // 保存分解记录
                saveBreakdownRecord(containerComponent, subComponent, containerComponent.getQuantity());
            }
            
            result.put("subComponents", subComponents);
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
        
        // 按component_no合并计算数量
        Map<String, Map<String, Object>> componentSummary = new HashMap<>();
        
        for (ContainerComponentsBreakdown breakdown : allBreakdowns) {
            Components component = breakdown.getSubComponent();
            String componentCode = component.getComponentCode();
            
            if (componentSummary.containsKey(componentCode)) {
                Map<String, Object> existing = componentSummary.get(componentCode);
                Integer currentQuantity = (Integer) existing.get("totalQuantity");
                existing.put("totalQuantity", currentQuantity + breakdown.getQuantity());
            } else {
                Map<String, Object> componentInfo = new HashMap<>();
                componentInfo.put("componentCode", componentCode);
                componentInfo.put("name", component.getName());
                componentInfo.put("procurementFlag", component.getProcurementFlag());
                componentInfo.put("commonPartsFlag", component.getCommonPartsFlag());
                componentInfo.put("totalQuantity", breakdown.getQuantity());
                componentSummary.put(componentCode, componentInfo);
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("contractId", contractId);
        summary.put("componentSummary", componentSummary.values());
        summary.put("totalComponents", componentSummary.size());
        
        return summary;
    }
}

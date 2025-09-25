package com.mms.service.impl;

import com.mms.entity.*;
import com.mms.repository.*;
import com.mms.service.BreakdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.io.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class BreakdownServiceImpl implements BreakdownService {
    
    private final ContainersRepository containersRepository;
    private final ContainerComponentsRepository containerComponentsRepository;
    private final ComponentsRepository componentsRepository;
    private final ComponentsRelationshipRepository componentsRelationshipRepository;
    private final ContainerComponentsBreakdownRepository breakdownRepository;
    private final ContainersComponentsSummaryRepository containersComponentsSummaryRepository;
    private final ContainerComponentsBreakdownProblemsRepository problemsRepository;
    private final ContractsRepository contractsRepository;
    
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
                Optional<Components> componentOpt = componentsRepository.findByComponentCode(containerComponent.getComponentNo());
                if (componentOpt.isEmpty()) {
                    // 保存问题部件到问题件表
                    ContainerComponentsBreakdownProblems problem = new ContainerComponentsBreakdownProblems();
                    problem.setContainer(container);
                    problem.setComponentNo(containerComponent.getComponentNo());
                    problem.setQuantity(containerComponent.getQuantity());
                    problem.setEntryTs(java.time.LocalDateTime.now());
                    problem.setEntryUser("SYS_USER");
                    problem.setLastUpdateTs(java.time.LocalDateTime.now());
                    problem.setLastUpdateUser("SYS_USER");
                    problemsRepository.save(problem);
                    
                    log.warn("保存问题部件到问题件表: containerId={}, componentNo={}, quantity={}", 
                        containerId, containerComponent.getComponentNo(), containerComponent.getQuantity());
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
        
        // 创建扁平的组件列表，包含所有组件（父组件和子组件）
        Map<String, Map<String, Object>> allComponents = new HashMap<>();
        List<String> problemComponents = new ArrayList<>();
        
        // 首先添加父组件（无论是否有分解结果都要显示）
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
                    componentInfo.put("remark", ""); // 正常组件无备注
                } else {
                    // 如果找不到匹配的组件，使用默认值并记录为问题部件
                    componentInfo.put("procurementFlag", false);
                    componentInfo.put("commonPartsFlag", false);
                    componentInfo.put("remark", "工件不存在"); // 问题组件备注
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
    
    @Override
    @Transactional
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
        
        // 获取所有箱包的分解数据
        Map<String, Map<String, Object>> mergedComponents = new HashMap<>();
        int totalContainers = 0;
        
        for (Integer containerId : containerIds) {
            Containers container = containersRepository.findById(containerId.longValue())
                .orElseThrow(() -> new RuntimeException("箱包不存在: " + containerId));
            
            // 获取该箱包的分解数据
            Map<String, Object> breakdownData = getContainerBreakdown(containerId.longValue());
            @SuppressWarnings("unchecked")
            Collection<Map<String, Object>> components = (Collection<Map<String, Object>>) breakdownData.get("allComponents");
            
            // 合并组件数据
            for (Map<String, Object> component : components) {
                String componentCode = (String) component.get("componentCode");
                Integer quantity = (Integer) component.get("quantity");
                
                if (mergedComponents.containsKey(componentCode)) {
                    // 累加数量
                    Map<String, Object> existing = mergedComponents.get(componentCode);
                    Integer currentQuantity = (Integer) existing.get("quantity");
                    existing.put("quantity", currentQuantity + quantity);
                } else {
                    // 新组件
                    Map<String, Object> newComponent = new HashMap<>(component);
                    mergedComponents.put(componentCode, newComponent);
                }
            }
            
            totalContainers++;
        }
        
        // 保存到containers_components_summary表
        Long contractId = containers.get(0).getContract().getId();
        
        // 先删除该合同之前创建的合并表
        log.info("删除合同 {} 之前的合并分解表", contractId);
        containersComponentsSummaryRepository.deleteByContractId(contractId);
        
        // 保存正常部件到合并表
        for (Map<String, Object> component : mergedComponents.values()) {
            String componentCode = (String) component.get("componentCode");
            Integer quantity = (Integer) component.get("quantity");
            String remark = (String) component.get("remark");
            
            // 只处理正常部件（在components表中存在的部件）
            if (remark == null || remark.isEmpty()) {
                Optional<Components> componentOpt = componentsRepository.findByComponentCode(componentCode);
                if (componentOpt.isPresent()) {
                    Components comp = componentOpt.get();
                    
                    // 创建新记录（因为已经删除了之前的记录）
                    ContainersComponentsSummary summary = new ContainersComponentsSummary();
                    // 设置关联对象而不是直接设置ID
                    Contracts contract = contractsRepository.findById(contractId).orElseThrow();
                    summary.setContract(contract);
                    summary.setComponent(comp);
                    summary.setContainer(null); // 合并表不关联特定箱包
                    summary.setQuantity(quantity);
                    containersComponentsSummaryRepository.save(summary);
                }
            }
        }
        
        // 合并问题部件：从各个箱包的问题件表中读取并合并
        Map<String, Integer> mergedProblems = new HashMap<>();
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
        
        // 保存合并后的问题部件到问题件表（使用第一个container作为关联）
        if (!mergedProblems.isEmpty()) {
            Containers firstContainer = containers.get(0);
            for (Map.Entry<String, Integer> entry : mergedProblems.entrySet()) {
                ContainerComponentsBreakdownProblems mergedProblem = new ContainerComponentsBreakdownProblems();
                mergedProblem.setContainer(firstContainer);
                mergedProblem.setComponentNo(entry.getKey());
                mergedProblem.setQuantity(entry.getValue());
                mergedProblem.setEntryTs(java.time.LocalDateTime.now());
                mergedProblem.setEntryUser("SYS_USER");
                mergedProblem.setLastUpdateTs(java.time.LocalDateTime.now());
                mergedProblem.setLastUpdateUser("SYS_USER");
                problemsRepository.save(mergedProblem);
                
                log.info("保存合并问题部件: componentNo={}, quantity={}", entry.getKey(), entry.getValue());
            }
        }
        
        // 生成PDF下载链接
        String downloadUrl = generateMergedBreakdownPdf(contractId, mergedComponents);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "合并分解表成功");
        result.put("totalContainers", totalContainers);
        result.put("totalComponents", mergedComponents.size());
        result.put("downloadUrl", downloadUrl);
        
        log.info("合并分解表完成: contractId={}, totalContainers={}, totalComponents={}", 
            contractId, totalContainers, mergedComponents.size());
        
        return result;
    }
    
    /**
     * 生成合并分解表PDF
     */
    private String generateMergedBreakdownPdf(Long contractId, Map<String, Map<String, Object>> mergedComponents) {
        // 暂时返回一个模拟的下载链接
        return "/api/breakdown/merged/" + contractId + "/download";
    }
    
    @Override
    public byte[] generateMergedBreakdownPdf(Long contractId) {
        log.info("生成合并分解表PDF: contractId={}", contractId);
        
        try {
            // 获取合同的合并分解数据
            List<ContainersComponentsSummary> summaries = containersComponentsSummaryRepository.findByContractId(contractId);
            List<ContainerComponentsBreakdownProblems> problems = problemsRepository.findByContractId(contractId);
            
            if (summaries.isEmpty() && problems.isEmpty()) {
                throw new RuntimeException("没有找到合并分解数据");
            }
            
            // 获取合同信息
            Optional<Contracts> contractOpt = contractsRepository.findById(contractId);
            if (contractOpt.isEmpty()) {
                throw new RuntimeException("合同不存在");
            }
            Contracts contract = contractOpt.get();
            
            // 创建PDF文档
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
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
            Table table = new Table(7).useAllAvailableWidth();
            table.setFont(font).setFontSize(10);
            
            // 添加表头
            table.addHeaderCell(new Cell().add(new Paragraph("序号").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("所属箱包").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("部件编号").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("部件名称").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("数量").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("是否外购").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("备注").setFont(boldFont)).setTextAlignment(TextAlignment.CENTER));
            
            // 添加正常部件数据行
            int index = 1;
            for (ContainersComponentsSummary summary : summaries) {
                Components component = summary.getComponent();
                
                table.addCell(new Cell().add(new Paragraph(String.valueOf(index))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("合并表"))); // 合并表中的正常部件
                table.addCell(new Cell().add(new Paragraph(component.getComponentCode())));
                table.addCell(new Cell().add(new Paragraph(component.getName())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(summary.getQuantity()))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(component.getProcurementFlag() ? "是" : "否")).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("")).setTextAlignment(TextAlignment.CENTER)); // 正常部件无备注
                
                index++;
            }
            
            // 添加问题部件数据行
            for (ContainerComponentsBreakdownProblems problem : problems) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(index))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(problem.getContainer().getName()))); // 问题部件所属箱包名称
                table.addCell(new Cell().add(new Paragraph(problem.getComponentNo())));
                table.addCell(new Cell().add(new Paragraph("未知部件"))); // 问题部件没有名称
                table.addCell(new Cell().add(new Paragraph(String.valueOf(problem.getQuantity()))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("未知")).setTextAlignment(TextAlignment.CENTER)); // 问题部件外购状态未知
                table.addCell(new Cell().add(new Paragraph("工件不存在")).setTextAlignment(TextAlignment.CENTER)); // 问题部件备注
                
                index++;
            }
            
            document.add(table);
            
            // 添加统计信息
            int totalNormalComponents = summaries.size();
            int totalProblemComponents = problems.size();
            int totalComponents = totalNormalComponents + totalProblemComponents;
            int totalNormalQuantity = summaries.stream().mapToInt(ContainersComponentsSummary::getQuantity).sum();
            int totalProblemQuantity = problems.stream().mapToInt(ContainerComponentsBreakdownProblems::getQuantity).sum();
            int totalQuantity = totalNormalQuantity + totalProblemQuantity;
            
            Paragraph stats = new Paragraph()
                .setFont(font)
                .setFontSize(12)
                .add("\n统计信息:\n")
                .add("正常部件数: " + totalNormalComponents + "\n")
                .add("问题部件数: " + totalProblemComponents + "\n")
                .add("总部件数: " + totalComponents + "\n")
                .add("正常部件总数量: " + totalNormalQuantity + "\n")
                .add("问题部件总数量: " + totalProblemQuantity + "\n")
                .add("总数量: " + totalQuantity)
                .setMarginTop(20);
            document.add(stats);
            
            document.close();
            
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("生成合并分解表PDF失败: contractId={}, error={}", contractId, e.getMessage(), e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage());
        }
    }
}

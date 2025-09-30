package com.mms.service;

import com.mms.entity.*;
import com.mms.repository.*;
import com.mms.service.impl.BreakdownServiceImpl;
import com.mms.utils.FastenerErpCodeFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工艺分解服务测试 - 验证ERP代码自动生成功能
 */
@ExtendWith(MockitoExtension.class)
public class BreakdownServiceErpIntegrationTest {
    
    @Mock
    private ContainersRepository containersRepository;
    
    @Mock
    private ContainerComponentsRepository containerComponentsRepository;
    
    @Mock
    private ComponentsRepository componentsRepository;
    
    @Mock
    private ComponentsRelationshipRepository componentsRelationshipRepository;
    
    @Mock
    private ContainerComponentsBreakdownRepository breakdownRepository;
    
    @Mock
    private ContainersComponentsSummaryRepository containersComponentsSummaryRepository;
    
    @Mock
    private ContainerComponentsBreakdownProblemsRepository problemsRepository;
    
    @Mock
    private ContractsRepository contractsRepository;
    
    @Mock
    private ComponentCacheService componentCacheService;
    
    @Mock
    private ContainerComponentsBreakdownErpService breakdownErpService;
    
    @Mock
    private FastenerErpCodeFinder fastenerErpCodeFinder;
    
    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    
    @InjectMocks
    private BreakdownServiceImpl breakdownService;
    
    private Containers sampleContainer;
    private ContainerComponents sampleContainerComponent;
    private Components sampleComponent;
    private ContainerComponentsBreakdown sampleBreakdown;
    
    @BeforeEach
    void setUp() {
        // 创建测试用的容器
        sampleContainer = new Containers();
        sampleContainer.setId(1L);
        sampleContainer.setName("测试箱包");
        sampleContainer.setStatus(0);
        
        // 创建测试用的容器组件
        sampleContainerComponent = new ContainerComponents();
        sampleContainerComponent.setId(1L);
        sampleContainerComponent.setContainer(sampleContainer);
        sampleContainerComponent.setQuantity(2);
        
        // 创建测试用的组件
        sampleComponent = new Components();
        sampleComponent.setId(1L);
        sampleComponent.setComponentCode("GB5783-M6*20-8.8Z");
        sampleComponent.setName("螺栓");
        
        // 创建测试用的分解记录
        sampleBreakdown = new ContainerComponentsBreakdown();
        sampleBreakdown.setId(1L);
        sampleBreakdown.setContainerComponent(sampleContainerComponent);
        sampleBreakdown.setSubComponent(sampleComponent);
        sampleBreakdown.setContainer(sampleContainer);
        sampleBreakdown.setQuantity(2);
    }
    
    @Test
    void testSaveBreakdownRecord_WithFastenerComponent_Success() {
        // 测试紧固件组件成功匹配的情况
        when(breakdownRepository.save(any(ContainerComponentsBreakdown.class))).thenReturn(sampleBreakdown);
        
        // 模拟FastenerErpCodeFinder返回成功结果
        FastenerErpCodeFinder.ErpCodeResult successResult = FastenerErpCodeFinder.ErpCodeResult.success(
                1L, "GB5783-M6*20-8.8Z", "螺栓", "07.0100.00001",
                "GB5783", "M6*20", "8.8", "镀锌等");
        when(fastenerErpCodeFinder.findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓"))
                .thenReturn(successResult);
        
        // 模拟breakdownErpService.create方法
        ContainerComponentsBreakdownErp expectedErpRecord = new ContainerComponentsBreakdownErp();
        expectedErpRecord.setBreakdown(sampleBreakdown);
        expectedErpRecord.setErpCode("07.0100.00001");
        expectedErpRecord.setComments("自动生成 - 匹配产品代码: GB5783, 规格: M6*20, 等级: 8.8, 表面处理: 镀锌等");
        when(breakdownErpService.create(any(ContainerComponentsBreakdownErp.class))).thenReturn(expectedErpRecord);
        
        // 使用反射调用私有方法saveBreakdownRecord
        try {
            java.lang.reflect.Method method = BreakdownServiceImpl.class.getDeclaredMethod(
                    "saveBreakdownRecord", ContainerComponents.class, Components.class, Integer.class);
            method.setAccessible(true);
            method.invoke(breakdownService, sampleContainerComponent, sampleComponent, 2);
        } catch (Exception e) {
            fail("调用saveBreakdownRecord方法失败: " + e.getMessage());
        }
        
        // 验证调用
        verify(breakdownRepository).save(any(ContainerComponentsBreakdown.class));
        verify(fastenerErpCodeFinder).findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓");
        verify(breakdownErpService).create(any(ContainerComponentsBreakdownErp.class));
    }
    
    @Test
    void testSaveBreakdownRecord_WithFastenerComponent_Failure() {
        // 测试紧固件组件查找失败的情况
        when(breakdownRepository.save(any(ContainerComponentsBreakdown.class))).thenReturn(sampleBreakdown);
        
        // 模拟FastenerErpCodeFinder返回失败结果
        FastenerErpCodeFinder.ErpCodeResult failureResult = FastenerErpCodeFinder.ErpCodeResult.unknownMaterial(
                1L, "GB5783-M6*20-8.8Z", "螺栓", "GB5783");
        when(fastenerErpCodeFinder.findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓"))
                .thenReturn(failureResult);
        
        // 模拟breakdownErpService.create方法
        ContainerComponentsBreakdownErp expectedErpRecord = new ContainerComponentsBreakdownErp();
        expectedErpRecord.setBreakdown(sampleBreakdown);
        expectedErpRecord.setErpCode(null);
        expectedErpRecord.setComments("未知物料: GB5783");
        when(breakdownErpService.create(any(ContainerComponentsBreakdownErp.class))).thenReturn(expectedErpRecord);
        
        // 使用反射调用私有方法saveBreakdownRecord
        try {
            java.lang.reflect.Method method = BreakdownServiceImpl.class.getDeclaredMethod(
                    "saveBreakdownRecord", ContainerComponents.class, Components.class, Integer.class);
            method.setAccessible(true);
            method.invoke(breakdownService, sampleContainerComponent, sampleComponent, 2);
        } catch (Exception e) {
            fail("调用saveBreakdownRecord方法失败: " + e.getMessage());
        }
        
        // 验证调用
        verify(breakdownRepository).save(any(ContainerComponentsBreakdown.class));
        verify(fastenerErpCodeFinder).findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓");
        verify(breakdownErpService).create(any(ContainerComponentsBreakdownErp.class));
    }
    
    @Test
    void testSaveBreakdownRecord_WithNonFastenerComponent() {
        // 测试非紧固件组件的情况
        when(breakdownRepository.save(any(ContainerComponentsBreakdown.class))).thenReturn(sampleBreakdown);
        
        // 模拟FastenerErpCodeFinder返回非紧固件结果
        FastenerErpCodeFinder.ErpCodeResult notFastenerResult = FastenerErpCodeFinder.ErpCodeResult.notFastener(
                1L, "FT001", "非紧固件");
        when(fastenerErpCodeFinder.findErpCode(1L, "FT001", "非紧固件"))
                .thenReturn(notFastenerResult);
        
        // 修改测试组件为非紧固件
        Components nonFastenerComponent = new Components();
        nonFastenerComponent.setId(1L);
        nonFastenerComponent.setComponentCode("FT001");
        nonFastenerComponent.setName("非紧固件");
        
        // 使用反射调用私有方法saveBreakdownRecord
        try {
            java.lang.reflect.Method method = BreakdownServiceImpl.class.getDeclaredMethod(
                    "saveBreakdownRecord", ContainerComponents.class, Components.class, Integer.class);
            method.setAccessible(true);
            method.invoke(breakdownService, sampleContainerComponent, nonFastenerComponent, 2);
        } catch (Exception e) {
            fail("调用saveBreakdownRecord方法失败: " + e.getMessage());
        }
        
        // 验证调用
        verify(breakdownRepository).save(any(ContainerComponentsBreakdown.class));
        verify(fastenerErpCodeFinder).findErpCode(1L, "FT001", "非紧固件");
        // 非紧固件组件不应该调用breakdownErpService.create
        verify(breakdownErpService, never()).create(any(ContainerComponentsBreakdownErp.class));
    }
    
    @Test
    void testSaveBreakdownRecord_WithException() {
        // 测试异常情况
        when(breakdownRepository.save(any(ContainerComponentsBreakdown.class))).thenReturn(sampleBreakdown);
        
        // 模拟FastenerErpCodeFinder抛出异常
        when(fastenerErpCodeFinder.findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓"))
                .thenThrow(new RuntimeException("测试异常"));
        
        // 模拟breakdownErpService.create方法
        ContainerComponentsBreakdownErp expectedErpRecord = new ContainerComponentsBreakdownErp();
        expectedErpRecord.setBreakdown(sampleBreakdown);
        expectedErpRecord.setErpCode(null);
        expectedErpRecord.setComments("ERP代码查找异常: 测试异常");
        when(breakdownErpService.create(any(ContainerComponentsBreakdownErp.class))).thenReturn(expectedErpRecord);
        
        // 使用反射调用私有方法saveBreakdownRecord
        try {
            java.lang.reflect.Method method = BreakdownServiceImpl.class.getDeclaredMethod(
                    "saveBreakdownRecord", ContainerComponents.class, Components.class, Integer.class);
            method.setAccessible(true);
            method.invoke(breakdownService, sampleContainerComponent, sampleComponent, 2);
        } catch (Exception e) {
            fail("调用saveBreakdownRecord方法失败: " + e.getMessage());
        }
        
        // 验证调用
        verify(breakdownRepository).save(any(ContainerComponentsBreakdown.class));
        verify(fastenerErpCodeFinder).findErpCode(1L, "GB5783-M6*20-8.8Z", "螺栓");
        verify(breakdownErpService).create(any(ContainerComponentsBreakdownErp.class));
    }
}

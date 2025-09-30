package com.mms.service;

import com.mms.entity.ContainerComponentsBreakdownErp;
import com.mms.service.impl.ContainerComponentsBreakdownErpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工艺分解ERP代码服务测试
 */
@ExtendWith(MockitoExtension.class)
public class ContainerComponentsBreakdownErpServiceTest {
    
    @Mock
    private com.mms.repository.ContainerComponentsBreakdownErpRepository erpRepository;
    
    @Mock
    private com.mms.repository.ContainerComponentsBreakdownRepository breakdownRepository;
    
    @Mock
    private com.mms.utils.FastenerErpCodeFinder fastenerErpCodeFinder;
    
    @InjectMocks
    private ContainerComponentsBreakdownErpServiceImpl erpService;
    
    private ContainerComponentsBreakdownErp sampleErpRecord;
    
    @BeforeEach
    void setUp() {
        // 创建测试用的工艺分解记录
        com.mms.entity.ContainerComponentsBreakdown breakdown = new com.mms.entity.ContainerComponentsBreakdown();
        breakdown.setId(1L);
        
        // 创建测试用的ERP代码记录
        sampleErpRecord = new ContainerComponentsBreakdownErp();
        sampleErpRecord.setId(1L);
        sampleErpRecord.setBreakdown(breakdown);
        sampleErpRecord.setErpCode("07.0100.00001");
        sampleErpRecord.setComments("测试ERP代码记录");
    }
    
    @Test
    void testCreate() {
        // 测试创建ERP代码记录
        when(erpRepository.save(any(ContainerComponentsBreakdownErp.class))).thenReturn(sampleErpRecord);
        
        ContainerComponentsBreakdownErp result = erpService.create(sampleErpRecord);
        
        assertNotNull(result);
        assertEquals("07.0100.00001", result.getErpCode());
        assertEquals("测试ERP代码记录", result.getComments());
        
        verify(erpRepository).save(sampleErpRecord);
    }
    
    @Test
    void testFindById() {
        // 测试根据ID查找
        when(erpRepository.findById(1L)).thenReturn(Optional.of(sampleErpRecord));
        
        Optional<ContainerComponentsBreakdownErp> result = erpService.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals("07.0100.00001", result.get().getErpCode());
        
        verify(erpRepository).findById(1L);
    }
    
    @Test
    void testFindByIdNotFound() {
        // 测试查找不存在的记录
        when(erpRepository.findById(999L)).thenReturn(Optional.empty());
        
        Optional<ContainerComponentsBreakdownErp> result = erpService.findById(999L);
        
        assertFalse(result.isPresent());
        
        verify(erpRepository).findById(999L);
    }
    
    @Test
    void testUpdate() {
        // 测试更新ERP代码记录
        sampleErpRecord.setId(1L);
        when(erpRepository.save(any(ContainerComponentsBreakdownErp.class))).thenReturn(sampleErpRecord);
        
        ContainerComponentsBreakdownErp result = erpService.update(sampleErpRecord);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("07.0100.00001", result.getErpCode());
        
        verify(erpRepository).save(sampleErpRecord);
    }
    
    @Test
    void testDeleteById() {
        // 测试删除ERP代码记录
        doNothing().when(erpRepository).deleteById(1L);
        
        erpService.deleteById(1L);
        
        verify(erpRepository).deleteById(1L);
    }
    
    @Test
    void testFindByBreakdownId() {
        // 测试根据工艺分解ID查找
        List<ContainerComponentsBreakdownErp> expectedRecords = Arrays.asList(sampleErpRecord);
        when(erpRepository.findByBreakdownId(1L)).thenReturn(expectedRecords);
        
        List<ContainerComponentsBreakdownErp> result = erpService.findByBreakdownId(1L);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("07.0100.00001", result.get(0).getErpCode());
        
        verify(erpRepository).findByBreakdownId(1L);
    }
    
    @Test
    void testFindByErpCode() {
        // 测试根据ERP代码查找
        List<ContainerComponentsBreakdownErp> expectedRecords = Arrays.asList(sampleErpRecord);
        when(erpRepository.findByErpCode("07.0100.00001")).thenReturn(expectedRecords);
        
        List<ContainerComponentsBreakdownErp> result = erpService.findByErpCode("07.0100.00001");
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("07.0100.00001", result.get(0).getErpCode());
        
        verify(erpRepository).findByErpCode("07.0100.00001");
    }
    
    @Test
    void testBatchCreate() {
        // 测试批量创建
        List<ContainerComponentsBreakdownErp> records = Arrays.asList(sampleErpRecord);
        when(erpRepository.saveAll(records)).thenReturn(records);
        
        List<ContainerComponentsBreakdownErp> result = erpService.batchCreate(records);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("07.0100.00001", result.get(0).getErpCode());
        
        verify(erpRepository).saveAll(records);
    }
    
    @Test
    void testCountByBreakdownId() {
        // 测试统计记录数量
        when(erpRepository.countByBreakdownId(1L)).thenReturn(5L);
        
        long count = erpService.countByBreakdownId(1L);
        
        assertEquals(5L, count);
        
        verify(erpRepository).countByBreakdownId(1L);
    }
    
    @Test
    void testBatchDeleteByBreakdownId() {
        // 测试批量删除
        doNothing().when(erpRepository).deleteByBreakdownId(1L);
        
        erpService.batchDeleteByBreakdownId(1L);
        
        verify(erpRepository).deleteByBreakdownId(1L);
    }
}

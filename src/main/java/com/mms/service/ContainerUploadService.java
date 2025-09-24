package com.mms.service;

import com.mms.entity.Containers;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ContainerUploadService {
    
    /**
     * 预览Excel文件内容（不保存到数据库）
     */
    Map<String, Object> previewExcelFile(MultipartFile file);
    
    /**
     * 解析Excel装箱单文件并创建装箱单
     */
    List<Containers> parseAndCreateContainers(Long contractId, MultipartFile file);
    
    /**
     * 根据合同参数查找类似的装箱单
     */
    List<Containers> findSimilarContainers(Long contractId);
    
    /**
     * 克隆装箱单到指定合同
     */
    List<Containers> cloneContainers(Long sourceContractId, Long targetContractId);
    
    /**
     * 根据合同ID获取装箱单列表
     */
    List<Containers> getContainersByContract(Long contractId);
    
    /**
     * 删除装箱单
     */
    void deleteContainer(Long containerId);
}

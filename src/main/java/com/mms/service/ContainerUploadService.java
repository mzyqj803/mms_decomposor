package com.mms.service;

import com.mms.entity.Containers;
import com.mms.entity.ContainerComponents;
import com.mms.entity.Contracts;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContainerUploadService {
    
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
}

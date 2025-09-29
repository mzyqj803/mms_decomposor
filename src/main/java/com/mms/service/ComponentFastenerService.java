package com.mms.service;

import java.util.List;

/**
 * 工件装配类型服务接口
 * 提供查询工件装配类型的功能
 */
public interface ComponentFastenerService {
    
    /**
     * 获取所有产线装配紧固件ID列表
     * @return 产线装配紧固件ID列表
     */
    List<Long> getAssembledFasteners();
    
    /**
     * 获取所有仓库装箱紧固件ID列表
     * @return 仓库装箱紧固件ID列表
     */
    List<Long> getUnassembledFasteners();
    
    /**
     * 检查指定工件是否为产线装配紧固件
     * @param componentId 工件ID
     * @return true-是产线装配，false-不是
     */
    boolean isAssembledFastener(Long componentId);
    
    /**
     * 检查指定工件是否为仓库装箱紧固件
     * @param componentId 工件ID
     * @return true-是仓库装箱，false-不是
     */
    boolean isUnassembledFastener(Long componentId);
    
    /**
     * 获取工件的装配类型
     * @param componentId 工件ID
     * @return 装配类型：null-无装配信息，"产线装配"-产线装配，"仓库装箱"-仓库装箱
     */
    String getAssemblyType(Long componentId);
}

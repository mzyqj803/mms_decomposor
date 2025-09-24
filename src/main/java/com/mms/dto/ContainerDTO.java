package com.mms.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerDTO {
    
    private Long id;
    private LocalDateTime entryTs;
    private String entryUser;
    private LocalDateTime lastUpdateTs;
    private String lastUpdateUser;
    
    // 合同信息
    private Long contractId;
    private String contractNo;
    private String clientName;
    private String projectName;
    
    // 箱包信息
    private String containerNo;
    private String name;
    private String containerSize;
    private String containerWeight;
    private String comments;
    
    /**
     * 从Containers实体转换为DTO
     */
    public static ContainerDTO fromEntity(com.mms.entity.Containers container) {
        if (container == null) {
            return null;
        }
        
        return ContainerDTO.builder()
                .id(container.getId())
                .entryTs(container.getEntryTs())
                .entryUser(container.getEntryUser())
                .lastUpdateTs(container.getLastUpdateTs())
                .lastUpdateUser(container.getLastUpdateUser())
                .contractId(container.getContract() != null ? container.getContract().getId() : null)
                .contractNo(container.getContract() != null ? container.getContract().getContractNo() : null)
                .clientName(container.getContract() != null ? container.getContract().getClientName() : null)
                .projectName(container.getContract() != null ? container.getContract().getProjectName() : null)
                .containerNo(container.getContainerNo())
                .name(container.getName())
                .containerSize(container.getContainerSize())
                .containerWeight(container.getContainerWeight())
                .comments(container.getComments())
                .build();
    }
}

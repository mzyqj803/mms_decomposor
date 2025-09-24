import request from './index'

export const breakdownApi = {
  // 对单个箱包进行工艺分解
  breakdownContainer(containerId) {
    return request({
      url: `/api/breakdown/container/${containerId}`,
      method: 'post'
    })
  },

  // 对合同的所有箱包进行工艺分解
  breakdownContract(contractId) {
    return request({
      url: `/api/breakdown/contract/${contractId}`,
      method: 'post'
    })
  },

  // 获取箱包的工艺分解结果
  getContainerBreakdown(containerId) {
    return request({
      url: `/api/breakdown/container/${containerId}`,
      method: 'get'
    })
  },

  // 获取合同的工艺分解汇总结果
  getContractBreakdownSummary(contractId) {
    return request({
      url: `/api/breakdown/contract/${contractId}/summary`,
      method: 'get'
    })
  },

  // 导出工艺分解表
  exportBreakdown(contractId, format = 'excel') {
    return request({
      url: `/api/breakdown/contract/${contractId}/export`,
      method: 'get',
      params: { format },
      responseType: 'blob'
    })
  }
}

import api from './index'

export const contractsApi = {
  // 获取合同列表
  getContracts(params = {}) {
    return api.get('/contracts', { params })
  },
  
  // 获取合同详情
  getContract(id) {
    return api.get(`/contracts/${id}`)
  },
  
  // 创建合同
  createContract(data) {
    return api.post('/contracts', data)
  },
  
  // 更新合同
  updateContract(id, data) {
    return api.put(`/contracts/${id}`, data)
  },
  
  // 删除合同
  deleteContract(id) {
    return api.delete(`/contracts/${id}`)
  },
  
  // 搜索合同
  searchContracts(keyword, page = 0, size = 10) {
    return api.get('/contracts/search', { 
      params: { 
        keyword,
        page,
        size
      } 
    })
  },
  
  // 生成装箱单
  generateContainer(id) {
    return api.post(`/contracts/${id}/containers/generate`)
  },
  
  
  // 开始工艺分解
  startBreakdown(id) {
    return api.post(`/contracts/${id}/breakdown/start`)
  },
  
  // 获取工艺分解结果
  getBreakdownResult(id) {
    return api.get(`/contracts/${id}/breakdown/result`)
  },
  
  // 导出分解表
  exportBreakdown(id, format = 'excel') {
    return api.get(`/contracts/${id}/breakdown/export`, {
      params: { format },
      responseType: 'blob'
    })
  },
  
  // 更新合同参数
  updateContractParameters(id, parameters) {
    return api.put(`/contracts/${id}/parameters/batch/replace`, parameters)
  },
  
  // 上传装箱单文件
  uploadContainerFile(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`/contracts/${id}/containers/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 查找类似的装箱单
  findSimilarContainers(id) {
    return api.get(`/contracts/${id}/containers/similar`)
  },
  
  // 克隆装箱单
  cloneContainers(id, sourceContractId) {
    return api.post(`/contracts/${id}/containers/clone`, null, {
      params: { sourceContractId }
    })
  },
  
  // 获取合同的所有箱包列表
  getContractContainers(id) {
    return api.get(`/contracts/${id}/containers`)
  }
}

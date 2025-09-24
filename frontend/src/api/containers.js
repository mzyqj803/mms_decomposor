import api from './index'

// 装箱单相关API
export const containersApi = {
  // 获取装箱单列表（支持搜索和分页）
  getContainers(params = {}) {
    return api.get('/containers', { params })
  },

  // 根据合同ID获取装箱单列表
  getContainersByContract(contractId) {
    return api.get(`/contracts/${contractId}/containers`)
  },

  // 预览Excel文件内容（不保存到数据库）
  previewExcelFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    
    return api.post('/containers/preview', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传装箱单Excel文件
  uploadContainer(contractId, file) {
    const formData = new FormData()
    formData.append('file', file)
    
    return api.post(`/contracts/${contractId}/containers/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 搜索相似装箱单
  searchSimilarContainers(contractId) {
    return api.get(`/contracts/${contractId}/containers/similar`)
  },

  // 克隆装箱单
  cloneContainer(contractId, sourceContractId) {
    return api.post(`/contracts/${contractId}/containers/clone`, null, {
      params: {
        sourceContractId
      }
    })
  },

  // 删除装箱单（通用API）
  deleteContainer(containerId) {
    return api.delete(`/containers/${containerId}`)
  },

  // 获取装箱单详情
  getContainerDetail(contractId, containerId) {
    return api.get(`/contracts/${contractId}/containers/${containerId}`)
  }
}

export default containersApi

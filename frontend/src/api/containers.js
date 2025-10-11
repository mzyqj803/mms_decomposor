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
  },

  // 获取装箱单内的组件列表
  getContainerComponents(containerId) {
    return api.get(`/containers/${containerId}/components`)
  },

  // 更新装箱单内的组件
  updateContainerComponent(containerId, componentId, componentData) {
    return api.put(`/containers/${containerId}/components/${componentId}`, componentData)
  },

  // 删除装箱单内的组件
  deleteContainerComponent(containerId, componentId) {
    return api.delete(`/containers/${containerId}/components/${componentId}`)
  }
}

export default containersApi

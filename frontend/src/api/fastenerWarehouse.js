import api from './index'

export const fastenerWarehouseApi = {
  // 获取紧固件列表（支持分页和多条件搜索）
  getFasteners(params = {}) {
    return api.get('/fastener-warehouse', { params })
  },
  
  // 获取紧固件详情
  getFastener(id) {
    return api.get(`/fastener-warehouse/${id}`)
  },
  
  // 创建紧固件
  createFastener(data) {
    return api.post('/fastener-warehouse', data)
  },
  
  // 更新紧固件
  updateFastener(id, data) {
    return api.put(`/fastener-warehouse/${id}`, data)
  },
  
  // 删除紧固件
  deleteFastener(id) {
    return api.delete(`/fastener-warehouse/${id}`)
  },
  
  // 搜索紧固件
  searchFasteners(keyword, params = {}) {
    return api.get('/fastener-warehouse/search', { 
      params: { keyword, ...params } 
    })
  },
  
  // 根据产品代码获取紧固件
  getFastenerByProductCode(productCode) {
    return api.get(`/fastener-warehouse/product-code/${productCode}`)
  },
  
  // 根据ERP代码获取紧固件
  getFastenerByErpCode(erpCode) {
    return api.get(`/fastener-warehouse/erp-code/${erpCode}`)
  },
  
  // 获取默认紧固件列表
  getDefaultFasteners() {
    return api.get('/fastener-warehouse/default')
  },
  
  // 设置默认紧固件
  setDefaultFastener(id) {
    return api.put(`/fastener-warehouse/${id}/default`)
  },
  
  // 获取所有材料列表
  getMaterials() {
    return api.get('/fastener-warehouse/materials')
  },
  
  // 获取所有表面处理列表
  getSurfaceTreatments() {
    return api.get('/fastener-warehouse/surface-treatments')
  },
  
  // 获取所有等级列表
  getLevels() {
    return api.get('/fastener-warehouse/levels')
  }
}

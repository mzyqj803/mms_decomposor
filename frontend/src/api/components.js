import api from './index'

export const componentsApi = {
  // 获取零部件列表（支持分页和搜索）
  getComponents(params = {}) {
    return api.get('/components', { params })
  },
  
  // 获取零部件详情
  getComponent(id) {
    return api.get(`/components/${id}`)
  },
  
  // 创建零部件
  createComponent(data) {
    return api.post('/components', data)
  },
  
  // 更新零部件
  updateComponent(id, data) {
    return api.put(`/components/${id}`, data)
  },
  
  // 删除零部件
  deleteComponent(id) {
    return api.delete(`/components/${id}`)
  },
  
  // 搜索零部件
  searchComponents(keyword) {
    return api.get('/components/search', { params: { keyword } })
  },
  
  // 获取零部件分类列表
  getComponentCategories() {
    return api.get('/components/categories')
  }
}

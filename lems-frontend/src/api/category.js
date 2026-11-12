
import request from './request'

const categoryApi = {

  // 查询全部分类
  getAll() {
    return request.get('/api/category')
  },

  // 查询分类详情
  getById(id) {
    return request.get(`/api/category/${id}`)
  },

  // 新增分类
  add(data) {
    return request.post('/api/category', data)
  },

  // 修改分类
  update(id, data) {
    return request.put(`/api/category/${id}`, data)
  },

  // 删除分类
  delete(id) {
    return request.delete(`/api/category/${id}`)
  }
}

export default categoryApi

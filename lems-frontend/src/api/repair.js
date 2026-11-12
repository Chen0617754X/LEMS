
import request from './request'

const repairApi = {

  // 查询全部维修记录（管理员）
  getAll() {
    return request.get('/api/repair')
  },

  // 根据 ID 查询维修详情
  getById(id) {
    return request.get(`/api/repair/${id}`)
  },

  // 查询当前用户自己的维修记录
  getMyRepairs() {
    return request.get('/api/repair/my')
  },

  // 查询当前用户自己的维修详情
  getMyRepairById(id) {
    return request.get(`/api/repair/${id}`)
  },

  // 新增维修记录
  add(data) {
    return request.post('/api/repair', data)
  },

  // 修改维修记录
  update(id, data) {
    return request.put(`/api/repair/${id}`, data)
  },

  // 删除维修记录
  delete(id) {
    return request.delete(`/api/repair/${id}`)
  },

  // 条件查询（管理员）
  search(params) {
    return request.get('/api/repair/search', {
      params
    })
  }
}

export default repairApi

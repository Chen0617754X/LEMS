
import request from './request'

// 设备管理接口
const equipmentApi = {

  // 查询全部设备
  getAll() {
    return request.get('/api/equipment')
  },

  // 根据 ID 查询设备详情
  getById(id) {
    return request.get(`/api/equipment/${id}`)
  },

  // 新增设备
  add(data) {
    return request.post('/api/equipment', data)
  },

  // 修改设备
  update(id, data) {
    return request.put(`/api/equipment/${id}`, data)
  },

  // 删除设备
  delete(id) {
    return request.delete(`/api/equipment/${id}`)
  },

  // 条件查询设备
  search(params) {
    return request.get('/api/equipment/search', {
      params
    })
  }
}

export default equipmentApi

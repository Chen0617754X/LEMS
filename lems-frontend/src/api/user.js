
import request from './request'

const userApi = {

  // 用户注册（管理员创建普通用户）
  register(data) {
    return request.post('/api/user/register', data)
  },

  // 查询当前登录用户
  getProfile() {
    return request.get('/api/user/profile')
  },

  // 查询全部用户（管理员）
  getAll() {
    return request.get('/api/user')
  },

  // 查询用户详情
  getById(id) {
    return request.get(`/api/user/${id}`)
  },

  // 修改用户基本信息
  update(id, data) {
    return request.put(`/api/user/${id}`, data)
  },

  // 删除用户
  delete(id) {
    return request.delete(`/api/user/${id}`)
  },

  // 修改用户状态
  updateStatus(id, status) {
    return request.put(`/api/user/${id}/status`, {
      status
    })
  }
}

export default userApi

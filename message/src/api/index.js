import axios from 'axios'
import store from '../store'

const request = axios.create({
  // 线上通过 Nginx 代理到后端，本地通过 vue.config.js devServer 代理
  baseURL: process.env.VUE_APP_API_BASE || '/message-board/api',
  timeout: 10000
})

// 请求拦截器：自动在请求头中携带UID
request.interceptors.request.use(
  config => {
    const uid = store.state.currentUid
    if (uid) {
      config.headers['X-User-Uid'] = uid
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    return Promise.reject(error)
  }
)

// ==================== 留言相关 API ====================

/** 分页查询留言列表 */
export function getMessageList(params) {
  return request.get('/message/list', { params })
}

/** 获取留言详情 */
export function getMessageDetail(id) {
  return request.get(`/message/detail/${id}`)
}

/** 发布留言 */
export function publishMessage(data) {
  return request.post('/message/publish', data)
}

/** 点赞/取消点赞 */
export function toggleLike(messageId) {
  return request.post('/message/like', { messageId })
}

/** 点赞热榜 */
export function getHotByLike(topN = 10) {
  return request.get('/message/hot/like', { params: { topN } })
}

/** 评论热榜 */
export function getHotByComment(topN = 10) {
  return request.get('/message/hot/comment', { params: { topN } })
}

// ==================== 评论相关 API ====================

/** 发表评论 */
export function publishComment(data) {
  return request.post('/comment/publish', data)
}

// ==================== 用户相关 API ====================

/** 获取当前登录用户信息 */
export function getCurrentUser() {
  return request.get('/user/current')
}

// ==================== 日志相关 API ====================

/** 上报访问主页日志（登录系统主页时调用一次） */
export function reportPageView() {
  return request.post('/log/page-view')
}

/** 获取服务器当前时间戳（毫秒），用于校准水印时间 */
export function getServerTime() {
  return request.get('/log/server-time')
}

// ==================== 文件上传 API ====================

/** 上传附件 */
export function uploadFiles(files) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

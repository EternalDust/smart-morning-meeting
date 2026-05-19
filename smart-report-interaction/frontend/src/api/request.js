import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  res => {
    const data = res.data
    if (data && data.success === false) {
      ElMessage.error(data.msg || '操作失败')
      return Promise.reject(new Error(data.msg))
    }
    return data
  },
  err => {
    ElMessage.error('网络请求失败')
    return Promise.reject(err)
  }
)

export default request

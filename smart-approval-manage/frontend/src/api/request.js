import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = 'Bearer ' + token
    return config
}, error => Promise.reject(error))

request.interceptors.response.use(response => {
    const res = response.data
    if (!res.success) return Promise.reject(new Error(res.msg || '请求失败'))
    return res.data
}, error => Promise.reject(error))

export default request
import http from '@ohos.net.http'
import { TokenManager } from '../utils/TokenManager'
import { ApiResponse } from '../../model/ApiResponse'

const BASE_URL = 'http://10.0.2.2:8085'

export class HttpClient {
  static async request<T>(method: http.RequestMethod, path: string, body?: Object): Promise<ApiResponse<T>> {
    const token = await TokenManager.getToken()
    const request = http.createHttp()
    const url = BASE_URL + path

    const options: http.HttpRequestOptions = {
      method: method,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      readTimeout: 15000,
      connectTimeout: 10000
    }

    if (body !== undefined) {
      options.extraData = JSON.stringify(body)
    }

    const response = await request.request(url, options)
    request.destroy()

    if (response.responseCode !== 200) {
      throw new Error(`HTTP ${response.responseCode}: ${response.result}`)
    }

    const json = JSON.parse(response.result as string) as ApiResponse<T>
    if (!json.success) {
      throw new Error(json.msg || '请求失败')
    }
    return json
  }

  static get<T>(path: string): Promise<ApiResponse<T>> {
    return HttpClient.request<T>(http.RequestMethod.GET, path)
  }

  static post<T>(path: string, body?: Object): Promise<ApiResponse<T>> {
    return HttpClient.request<T>(http.RequestMethod.POST, path, body)
  }

  static put<T>(path: string, body?: Object): Promise<ApiResponse<T>> {
    return HttpClient.request<T>(http.RequestMethod.PUT, path, body)
  }
}

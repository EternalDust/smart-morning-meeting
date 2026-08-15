import request from './request'

// 生成复盘报告 / 管理决策建议
export const generateReport = (data) => request.post('/ai/review-report', data)

// 获取最新报告
export const getLatestReport = (reportType) => request.get('/ai/review-report/latest', { params: { reportType } })

// 按时间范围查看历史报告
export const getReportList = (params) => request.get('/ai/review-report/list', { params })

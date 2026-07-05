import { HttpClient } from './HttpClient'
import { ApiResponse } from '../../model/ApiResponse'
import { SignRecord, SignStats } from '../../model/SignRecord'
import { SpeechRecord, MeetingSummary } from '../../model/SpeechRecord'
import { InteractionMessage } from '../../model/InteractionMessage'

export interface LoginData {
  token: string
  userId: string
  userName: string
}

export interface SignListData {
  records: SignRecord[]
  stats: SignStats
  nameMap: Record<number, string>
}

export interface SpeechListData {
  records: SpeechRecord[]
  nameMap: Record<number, string>
}

export interface InteractionListData {
  messages: InteractionMessage[]
  nameMap: Record<number, string>
}

export interface InteractionStats {
  questions: number
  feedback: number
  votes: number
  replied: number
}

export class ApiService {
  static login(userId: string, password: string) {
    return HttpClient.post<ApiResponse<LoginData>>('/api/auth/login', { userId, password })
  }

  static signIn(meetingId: number, userId: number, signType: number = 2) {
    return HttpClient.post<ApiResponse<SignRecord>>('/api/report/meeting/sign/in', { meetingId, userId, signType })
  }

  static getSignList(meetingId: number) {
    return HttpClient.get<ApiResponse<SignListData>>(`/api/report/meeting/sign/list/${meetingId}`)
  }

  static saveSpeech(meetingId: number, speakerId: number, content: string) {
    return HttpClient.post<ApiResponse<SpeechRecord>>('/api/report/meeting/speech/save', { meetingId, speakerId, content })
  }

  static getSpeechList(meetingId: number) {
    return HttpClient.get<ApiResponse<SpeechListData>>(`/api/report/meeting/speech/list/${meetingId}`)
  }

  static getSummary(meetingId: number) {
    return HttpClient.get<ApiResponse<MeetingSummary>>(`/api/report/meeting/summary/${meetingId}`)
  }

  static sendMessage(meetingId: number, userId: number, interactType: number, content: string) {
    return HttpClient.post<ApiResponse<InteractionMessage>>('/api/report/meeting/interaction/message', { meetingId, userId, interactType, content })
  }

  static replyMessage(messageId: number, reply: string) {
    return HttpClient.post<ApiResponse<InteractionMessage>>(`/api/report/meeting/interaction/reply/${messageId}`, { reply })
  }

  static getInteractionList(meetingId: number, type?: number) {
    let path = `/api/report/meeting/interaction/list/${meetingId}`
    if (type !== undefined && type > 0) path += `?type=${type}`
    return HttpClient.get<ApiResponse<InteractionListData>>(path)
  }

  static getInteractionStats(meetingId: number) {
    return HttpClient.get<ApiResponse<InteractionStats>>(`/api/report/meeting/interaction/stats/${meetingId}`)
  }

  static getMeetingAnalytics(meetingId: number) {
    return HttpClient.get<ApiResponse<Object>>(`/api/report/analytics/meeting/${meetingId}`)
  }

  static getWeeklyTrend() {
    return HttpClient.get<ApiResponse<Array<Object>>>('/api/report/analytics/trend/weekly')
  }

  static getTimePattern() {
    return HttpClient.get<ApiResponse<Array<Object>>>('/api/report/analytics/trend/time')
  }
}

import { HttpClient } from './HttpClient'
import { ApiResponse } from '../../model/ApiResponse'
import { SignRecord } from '../../model/SignRecord'
import { SpeechRecord } from '../../model/SpeechRecord'
import { InteractionMessage } from '../../model/InteractionMessage'

export interface LoginData {
  token: string
  userId: string
  userName: string
}

export interface SignListData {
  records: SignRecord[]
  nameMap: Record<number, string>
  shouldAttend: number
  normal: number
  late: number
  absent: number
  signed: number
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

export interface SummaryData {
  summary: string
  keyPoints: string[]
  decisions: string[]
  medicalEntities: string[]
}

interface LoginReq { userId: string; password: string }
interface SignInReq { meetingId: number; userId: number; signType: number }
interface SpeechReq { meetingId: number; speakerId: number; content: string }
interface MessageReq { meetingId: number; userId: number; interactType: number; content: string }
interface ReplyReq { reply: string }

export class ApiService {
  static login(userId: string, password: string): Promise<ApiResponse<LoginData>> {
    const body: LoginReq = { userId: userId, password: password }
    return HttpClient.post<LoginData>('/api/auth/login', body)
  }

  static signIn(meetingId: number, userId: number, signType: number = 2): Promise<ApiResponse<SignRecord>> {
    const body: SignInReq = { meetingId: meetingId, userId: userId, signType: signType }
    return HttpClient.post<SignRecord>('/api/report/meeting/sign/in', body)
  }

  static getSignList(meetingId: number): Promise<ApiResponse<SignListData>> {
    return HttpClient.get<SignListData>(`/api/report/meeting/sign/list/${meetingId}`)
  }

  static saveSpeech(meetingId: number, speakerId: number, content: string): Promise<ApiResponse<SpeechRecord>> {
    const body: SpeechReq = { meetingId: meetingId, speakerId: speakerId, content: content }
    return HttpClient.post<SpeechRecord>('/api/report/meeting/speech/save', body)
  }

  static getSpeechList(meetingId: number): Promise<ApiResponse<SpeechListData>> {
    return HttpClient.get<SpeechListData>(`/api/report/meeting/speech/list/${meetingId}`)
  }

  static getSummary(meetingId: number): Promise<ApiResponse<SummaryData>> {
    return HttpClient.get<SummaryData>(`/api/report/meeting/summary/${meetingId}`)
  }

  static generateSummary(meetingId: number): Promise<ApiResponse<SummaryData>> {
    return HttpClient.post<SummaryData>(`/api/report/meeting/summary/generate/${meetingId}`)
  }

  static sendMessage(meetingId: number, userId: number, interactType: number, content: string): Promise<ApiResponse<InteractionMessage>> {
    const body: MessageReq = { meetingId: meetingId, userId: userId, interactType: interactType, content: content }
    return HttpClient.post<InteractionMessage>('/api/report/meeting/interaction/message', body)
  }

  static replyMessage(messageId: number, reply: string): Promise<ApiResponse<InteractionMessage>> {
    const body: ReplyReq = { reply: reply }
    return HttpClient.post<InteractionMessage>(`/api/report/meeting/interaction/reply/${messageId}`, body)
  }

  static aiAnswer(messageId: number): Promise<ApiResponse<InteractionMessage>> {
    return HttpClient.post<InteractionMessage>(`/api/report/meeting/interaction/ai-reply/${messageId}`)
  }

  static getInteractionList(meetingId: number, type?: number): Promise<ApiResponse<InteractionListData>> {
    let path = `/api/report/meeting/interaction/list/${meetingId}`
    if (type !== undefined && type > 0) path += `?type=${type}`
    return HttpClient.get<InteractionListData>(path)
  }

  static getInteractionStats(meetingId: number): Promise<ApiResponse<InteractionStats>> {
    return HttpClient.get<InteractionStats>(`/api/report/meeting/interaction/stats/${meetingId}`)
  }

  static getMeetingAnalytics(meetingId: number): Promise<ApiResponse<Record<string, Object>>> {
    return HttpClient.get<Record<string, Object>>(`/api/report/analytics/meeting/${meetingId}`)
  }

  static getWeeklyTrend(): Promise<ApiResponse<Array<Record<string, Object>>>> {
    return HttpClient.get<Array<Record<string, Object>>>('/api/report/analytics/trend/weekly')
  }

  static getTimePattern(): Promise<ApiResponse<Array<Record<string, Object>>>> {
    return HttpClient.get<Array<Record<string, Object>>>('/api/report/analytics/trend/time')
  }
}

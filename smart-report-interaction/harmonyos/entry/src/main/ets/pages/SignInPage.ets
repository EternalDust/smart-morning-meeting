import { ApiService, SignListData } from '../common/api/ApiService'
import { StatCard } from '../common/components/StatCard'
import { EmptyState } from '../common/components/EmptyState'
import { SignRecord } from '../model/SignRecord'
import { TokenManager } from '../common/utils/TokenManager'
import webSocket from '@ohos.net.webSocket'

@Component
export struct SignInPageContent {
  @State shouldAttend: number = 0
  @State normal: number = 0
  @State late: number = 0
  @State absent: number = 0
  @State signed: number = 0
  @State records: SignRecord[] = []
  @State isLoading: boolean = false
  @State isSigning: boolean = false
  @State nameMap: Record<number, string> = {}
  @State userId: string = ''
  @State userName: string = ''
  @State alreadySigned: boolean = false
  @State qualityScore: number = 0
  @State attendRate: number = 0
  @State speechCount: number = 0
  @State interCount: number = 0
  @State isAnomaly: boolean = false
  @State isAdmin: boolean = false
  private ws: webSocket.WebSocket | null = null
  private wsDestroyed: boolean = false

  async aboutToAppear() {
    this.userId = await TokenManager.getUserId()
    this.userName = await TokenManager.getUserName()
    this.isAdmin = this.userId.startsWith('2')
    this.wsDestroyed = false
    await this.loadData()
    this.connectWebSocket()
  }

  aboutToDisappear() {
    this.wsDestroyed = true
    this.ws?.close()
    this.ws = null
  }

  async loadData() {
    this.isLoading = true
    try {
      const res = await ApiService.getSignList(1)
      const data = res.data as SignListData
      this.records = data.records
      this.shouldAttend = data.shouldAttend
      this.normal = data.normal; this.late = data.late
      this.absent = data.absent; this.signed = data.signed
      this.nameMap = data.nameMap
      if (this.userId) {
        this.alreadySigned = this.records.some(r => String(r.userId) === this.userId)
      }
    } catch (e) {} finally { this.isLoading = false }
    try {
      const a = await ApiService.getMeetingAnalytics(1)
      const d = a.data as Record<string, Object>
      this.qualityScore = d['qualityScore'] as number ?? 0
      this.attendRate = d['attendRate'] as number ?? 0
      this.speechCount = d['speechCount'] as number ?? 0
      this.interCount = d['interactionCount'] as number ?? 0
      this.isAnomaly = (d['isAnomaly'] as number ?? 0) === 1
    } catch (e) {}
  }

  connectWebSocket() {
    this.ws = webSocket.createWebSocket()
    this.ws.connect('ws://10.0.2.2:8081/api/meeting/realtime/push/1', (err: Error, value: string) => {})
    this.ws.on('message', (err: Error, value: string) => {
      try { const msg = JSON.parse(value as string); if (msg.type === 'sign') this.loadData() } catch (e) {}
    })
    this.ws.on('close', (err: Error, value: string) => {
      if (this.wsDestroyed) return
      setTimeout(() => this.connectWebSocket(), 3000)
    })
  }

  async doSignIn() {
    if (!this.userId) return
    this.isSigning = true
    try {
      await ApiService.signIn(1, parseInt(this.userId), 2)
      await this.loadData()
    } catch (e) {} finally { this.isSigning = false }
  }

  build() {
    Column() {
      Row() {
        StatCard({ label: '应到', value: this.shouldAttend, color: '#2563EB' })
        StatCard({ label: '准时', value: this.normal, color: '#059669' })
        StatCard({ label: '迟到', value: this.late, color: '#D97706' })
        StatCard({ label: '缺勤', value: this.absent, color: '#DC2626' })
      }.width('100%').padding(12).justifyContent(FlexAlign.SpaceAround)

      Column() {
        Text(this.userName || '参会用户').fontSize(18).fontWeight(FontWeight.Bold).fontColor('#1E293B')
        Text(this.isAdmin ? '管理员' : '参会人员').fontSize(12).fontColor('#475569').margin({ top: 2 })
      }.width('100%').padding({ top: 8, bottom: 8 }).alignItems(HorizontalAlign.Center)

      Button(this.alreadySigned ? '已签到' : '一键签到')
        .height(48).width('90%')
        .enabled(!this.isSigning && !this.alreadySigned)
        .backgroundColor(this.alreadySigned ? '#94A3B8' : '#2563EB')
        .borderRadius(8)
        .onClick(() => this.doSignIn())

      if (this.isAdmin && this.qualityScore > 0) {
        Column() {
          Text(`${this.qualityScore}`).fontSize(32).fontWeight(FontWeight.Bold).fontColor('#2563EB')
          Text('会议质量评分').fontSize(12).fontColor('#475569')
          Text(`出勤 ${this.attendRate}% · 发言 ${this.speechCount} · 互动 ${this.interCount}`)
            .fontSize(11).fontColor('#475569').margin({ top: 4 })
          if (this.isAnomaly) {
            Text('异常').fontSize(11).fontColor('#DC2626')
              .padding({ left: 8, right: 8, top: 2, bottom: 2 })
              .backgroundColor('#FEE2E2').borderRadius(4).margin({ top: 4 })
          }
        }.width('90%').padding(12).backgroundColor('#EFF6FF').borderRadius(12).margin({ top: 12 })
        .alignItems(HorizontalAlign.Center)
      }

      if (this.records.length === 0) {
        EmptyState({ message: '暂无签到记录' })
      } else {
        List() {
          ForEach(this.records, (item: SignRecord) => {
            ListItem() {
              Row() {
                Text(this.nameMap[item.userId] || String(item.userId))
                  .fontSize(14).fontWeight(FontWeight.Medium).fontColor('#1E293B')
                Blank()
                Text(item.signStatus === 0 ? '准时' : '迟到')
                  .fontSize(12)
                  .fontColor(item.signStatus === 0 ? '#059669' : '#D97706')
                  .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                  .backgroundColor(item.signStatus === 0 ? '#D1FAE5' : '#FEF3C7')
                  .borderRadius(4)
                Text(item.signTime.substring(11, 16))
                  .fontSize(12).fontColor('#475569').margin({ left: 8 })
              }
              .width('100%').height(48).padding({ left: 16, right: 16 })
              .backgroundColor('#FFFFFF').borderRadius(8)
              .margin({ top: 4, left: 12, right: 12 })
            }
          })
        }.layoutWeight(1)
      }
    }.width('100%').height('100%')
  }
}

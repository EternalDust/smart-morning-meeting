import { ApiService, InteractionListData } from '../common/api/ApiService'
import { EmptyState } from '../common/components/EmptyState'
import { InteractionMessage } from '../model/InteractionMessage'
import { TokenManager } from '../common/utils/TokenManager'
import webSocket from '@ohos.net.webSocket'

@Component
export struct InteractionPageContent {
  @State messages: InteractionMessage[] = []
  @State inputText: string = ''
  @State isLoading: boolean = false
  @State isSending: boolean = false
  @State nameMap: Record<number, string> = {}
  @State allAttendees: Record<number, string> = {}
  @State userId: string = ''
  @State userName: string = ''
  @State timePattern: Array<Record<string, Object>> = []
  @State showVote: boolean = false
  @State voteTitle: string = ''
  @State voteOptions: string = ''
  @State aiReplyingId: number = 0
  @State replyTargetId: number = 0
  @State replyText: string = ''
  private ws: webSocket.WebSocket | null = null
  private wsDestroyed: boolean = false

  async aboutToAppear() {
    this.userId = await TokenManager.getUserId()
    this.userName = await TokenManager.getUserName()
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
      const res = await ApiService.getInteractionList(1)
      const data = res.data as InteractionListData
      this.messages = data.messages
      this.nameMap = data.nameMap
    } finally { this.isLoading = false }
    try {
      const s = await ApiService.getSignList(1)
      const d = s.data as Record<string, Object>
      this.allAttendees = (d['nameMap'] as Record<number, string>) || {}
    } catch (e) {}
    try {
      const tp = await ApiService.getTimePattern()
      this.timePattern = tp.data as Array<Record<string, Object>> || []
    } catch (e) {}
  }

  connectWebSocket() {
    this.ws = webSocket.createWebSocket()
    this.ws.connect('ws://10.0.2.2:8081/api/meeting/realtime/push/1', (err: Error, value: string) => {})
    this.ws.on('message', (err: Error, value: string) => {
      try { const msg = JSON.parse(value as string); if (msg.type === 'interaction') this.loadData() } catch (e) {}
    })
    this.ws.on('close', (err: Error, value: string) => {
      if (this.wsDestroyed) return
      setTimeout(() => this.connectWebSocket(), 3000)
    })
  }

  async sendMessage() {
    if (!this.inputText.trim()) return
    if (!this.userId) return
    this.isSending = true
    try {
      await ApiService.sendMessage(1, parseInt(this.userId), 2, this.inputText.trim())
      this.inputText = ''
      await this.loadData()
    } finally { this.isSending = false }
  }

  async submitVote() {
    if (!this.voteTitle || !this.voteOptions.trim()) return
    if (!this.userId) return
    const options = this.voteOptions.split('\n').filter(o => o.trim())
    if (options.length < 2) return
    const content = `【投票】${this.voteTitle}\n` + options.map((o, i) => `${i + 1}. ${o}`).join('\n')
    try {
      await ApiService.sendMessage(1, parseInt(this.userId), 3, content)
      this.showVote = false; this.voteTitle = ''; this.voteOptions = ''
      await this.loadData()
    } catch (e) {}
  }

  async aiReply(item: InteractionMessage) {
    this.aiReplyingId = item.id
    try {
      await ApiService.aiAnswer(item.id)
      await this.loadData()
    } catch (e) {} finally { this.aiReplyingId = 0 }
  }

  openReply(item: InteractionMessage) {
    this.replyTargetId = item.id
    this.replyText = ''
  }

  async submitReply() {
    const text = this.replyText.trim()
    if (!text) return
    try {
      await ApiService.replyMessage(this.replyTargetId, text)
      this.replyTargetId = 0
      this.replyText = ''
      await this.loadData()
    } catch (e) {}
  }

  build() {
    Stack() {
      Column() {
        if (this.messages.length === 0) {
          EmptyState({ message: '暂无互动消息' })
        } else {
          List() {
            ForEach(this.messages, (item: InteractionMessage) => {
              ListItem() {
                Column() {
                  Row() {
                    Text(this.nameMap[item.userId] || this.allAttendees[item.userId] || `用户${item.userId}`)
                      .fontSize(14).fontWeight(FontWeight.Medium).fontColor('#1E293B')
                    if (item.interactType === 3) {
                      Text('投票').fontSize(10).fontColor('#D97706')
                        .padding({ left: 6, right: 6, top: 1, bottom: 1 })
                        .backgroundColor('#FEF3C7').borderRadius(4).margin({ left: 6 })
                    }
                    Blank()
                    Text(item.createTime?.substring(11, 16) || '')
                      .fontSize(11).fontColor('#94A3B8')
                  }
                  Text(item.content).fontSize(14).fontColor('#475569').margin({ top: 6 }).lineHeight(20)
                  if (!item.reply && this.userId.startsWith('2')) {
                    Row() {
                      Button(this.aiReplyingId === item.id ? '答复中...' : 'AI 答复')
                        .height(28).fontSize(12).margin({ top: 6 })
                        .enabled(this.aiReplyingId !== item.id)
                        .backgroundColor('#2563EB').borderRadius(6)
                        .onClick(() => this.aiReply(item))
                      Button('回复')
                        .height(28).fontSize(12).margin({ top: 6, left: 8 })
                        .backgroundColor('#10B981').borderRadius(6)
                        .onClick(() => this.openReply(item))
                    }.width('100%').alignItems(HorizontalAlign.Start)
                  }
                  if (item.reply) {
                    Text(`回复：${item.reply}`).fontSize(13).fontColor('#1E293B')
                      .margin({ top: 8 }).padding(10)
                      .backgroundColor('#F1F5F9').borderRadius(6).width('100%')
                  }
                }.padding(14).width('100%')
                .backgroundColor('#FFFFFF').borderRadius(8)
                .margin({ top: 6, left: 12, right: 12 })
              }
            })
          }.layoutWeight(1)
        }

        Row() {
          Text(this.userName || '用户').fontSize(13).fontColor('#2563EB').fontWeight(FontWeight.Medium)
          TextInput({ placeholder: '输入互动内容...', text: this.inputText })
            .height(44).layoutWeight(1)
            .backgroundColor('#F1F5F9').borderRadius(8).padding({ left: 12, right: 12 })
            .margin({ left: 8, right: 8 })
            .onChange((value: string) => this.inputText = value)
          Button('发送')
            .height(44).width(64)
            .enabled(!this.isSending && this.inputText.trim().length > 0)
            .backgroundColor(this.inputText.trim() ? '#2563EB' : '#CBD5E1')
            .borderRadius(8)
            .onClick(() => this.sendMessage())
          Button('投票')
            .height(44).width(64).margin({ left: 4 })
            .backgroundColor('#F59E0B').borderRadius(8)
            .onClick(() => this.showVote = true)
        }
        .width('100%').padding({ left: 12, right: 12, top: 8, bottom: 12 })
        .backgroundColor('#FFFFFF')
        .border({ width: { top: 1 }, color: '#E2E8F0' })
      }
      .width('100%').height('100%')

      if (this.showVote) {
        Column() {
          Column() {
            Text('发起投票').fontSize(16).fontWeight(FontWeight.Bold).margin({ bottom: 12 })
            TextInput({ placeholder: '投票标题', text: this.voteTitle })
              .height(44).width('100%').backgroundColor('#F1F5F9').borderRadius(8)
              .onChange((v: string) => this.voteTitle = v)
            TextArea({ placeholder: '每行一个选项', text: this.voteOptions })
              .height(100).width('100%').backgroundColor('#F1F5F9').borderRadius(8)
              .margin({ top: 8 })
              .onChange((v: string) => this.voteOptions = v)
            Row() {
              Button('取消').height(40).layoutWeight(1).backgroundColor('#F1F5F9').borderRadius(8)
                .onClick(() => this.showVote = false)
              Button('发起').height(40).layoutWeight(1).backgroundColor('#2563EB').borderRadius(8)
                .margin({ left: 8 })
                .onClick(() => this.submitVote())
            }.width('100%').margin({ top: 12 })
          }
          .padding(20).backgroundColor('#FFFFFF').borderRadius(12).width('85%')
        }
        .width('100%').height('100%')
        .backgroundColor('rgba(0,0,0,0.4)')
        .justifyContent(FlexAlign.Center)
        .onClick(() => this.showVote = false)
      }

      if (this.replyTargetId > 0) {
        Column() {
          Column() {
            Text('回复消息').fontSize(16).fontWeight(FontWeight.Bold).margin({ bottom: 12 })
            TextInput({ placeholder: '输入回复内容', text: this.replyText })
              .height(44).width('100%').backgroundColor('#F1F5F9').borderRadius(8)
              .onChange((v: string) => this.replyText = v)
            Row() {
              Button('取消').height(40).layoutWeight(1).backgroundColor('#F1F5F9').borderRadius(8)
                .onClick(() => { this.replyTargetId = 0; this.replyText = '' })
              Button('发送').height(40).layoutWeight(1).backgroundColor('#10B981').borderRadius(8)
                .margin({ left: 8 })
                .onClick(() => this.submitReply())
            }.width('100%').margin({ top: 12 })
          }
          .padding(20).backgroundColor('#FFFFFF').borderRadius(12).width('85%')
        }
        .width('100%').height('100%')
        .backgroundColor('rgba(0,0,0,0.4)')
        .justifyContent(FlexAlign.Center)
        .onClick(() => { this.replyTargetId = 0; this.replyText = '' })
      }
    }
    .width('100%').height('100%')
  }
}

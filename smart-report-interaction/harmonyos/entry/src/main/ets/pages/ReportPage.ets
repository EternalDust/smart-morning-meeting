import { ApiService, SpeechListData } from '../common/api/ApiService'
import { EmptyState } from '../common/components/EmptyState'
import { SpeechRecord } from '../model/SpeechRecord'
import { TokenManager } from '../common/utils/TokenManager'

@Component
export struct ReportPageContent {
  @State speeches: SpeechRecord[] = []
  @State aiSummary: string = ''
  @State aiKeyPoints: Array<string> = []
  @State aiDecisions: Array<string> = []
  @State aiEntities: Array<string> = []
  @State isGenerating: boolean = false
  @State isLoading: boolean = false
  @State nameMap: Record<number, string> = {}
  @State userName: string = ''
  @State qualityScore: number = 0
  @State attendRate: number = 0
  @State speechCount: number = 0
  @State interCount: number = 0
  @State weeklyTrend: Array<Record<string, Object>> = []

  async aboutToAppear() {
    this.userName = await TokenManager.getUserName()
    await this.loadData()
  }

  async loadData() {
    this.isLoading = true
    try {
      const [speechRes, summaryRes] = await Promise.all([
        ApiService.getSpeechList(1), ApiService.getSummary(1)
      ])
      const data = speechRes.data as SpeechListData
      this.speeches = data.records
      this.nameMap = data.nameMap
      this.applySummary(summaryRes.data)
    } finally { this.isLoading = false }
    try {
      const a = await ApiService.getMeetingAnalytics(1)
      const d = a.data as Record<string, Object>
      this.qualityScore = d['qualityScore'] as number ?? 0
      this.attendRate = d['attendRate'] as number ?? 0
      this.speechCount = d['speechCount'] as number ?? 0
      this.interCount = d['interactionCount'] as number ?? 0
    } catch (e) {}
    try {
      const w = await ApiService.getWeeklyTrend()
      this.weeklyTrend = (w.data as Array<Record<string, Object>>).slice(-4)
    } catch (e) {}
  }

  applySummary(data: Object | null) {
    const d = data as Record<string, Object>
    this.aiSummary = (d['summary'] as string) || ''
    this.aiKeyPoints = (d['keyPoints'] as Array<string>) || []
    this.aiDecisions = (d['decisions'] as Array<string>) || []
    this.aiEntities = (d['medicalEntities'] as Array<string>) || []
  }

  async generateAiSummary() {
    this.isGenerating = true
    try {
      const res = await ApiService.generateSummary(1)
      this.applySummary(res.data)
    } catch (e) {} finally { this.isGenerating = false }
  }

  build() {
    Column() {
      Column() {
        Text('语音实时转写').fontSize(15).fontWeight(FontWeight.Bold).fontColor('#2563EB')
        Text(`当前主讲人：${this.userName || '参会用户'}`)
          .fontSize(12).fontColor('#475569').margin({ top: 4 })
        Text('语音接入后自动转写为文字，AI 提取关键要点').fontSize(11).fontColor('#94A3B8').margin({ top: 4 })
      }
      .width('100%').padding(16).backgroundColor('#EFF6FF').borderRadius(8)
      .margin({ left: 12, right: 12, top: 12 })

      Column() {
        Row() {
          Text('AI 会议摘要').fontSize(14).fontWeight(FontWeight.Bold).fontColor('#2563EB')
          Blank()
          Button(this.isGenerating ? '生成中...' : 'AI 生成摘要')
            .height(32).fontSize(12)
            .enabled(!this.isGenerating)
            .backgroundColor('#2563EB').borderRadius(6)
            .onClick(() => this.generateAiSummary())
        }.width('100%')
        if (this.aiSummary) {
          Text(this.aiSummary).fontSize(13).fontColor('#475569').margin({ top: 6 }).lineHeight(20)
        }
        if (this.aiKeyPoints.length > 0) {
          Text('关键要点').fontSize(12).fontWeight(FontWeight.Medium).fontColor('#1E293B').margin({ top: 8 })
          ForEach(this.aiKeyPoints, (k: string, i: number) => {
            Text(`${i + 1}. ${k}`).fontSize(12).fontColor('#475569').margin({ top: 2 }).lineHeight(18)
          })
        }
        if (this.aiDecisions.length > 0) {
          Text('决策事项').fontSize(12).fontWeight(FontWeight.Medium).fontColor('#1E293B').margin({ top: 8 })
          ForEach(this.aiDecisions, (d: string) => {
            Text(`• ${d}`).fontSize(12).fontColor('#475569').margin({ top: 2 }).lineHeight(18)
          })
        }
        if (this.aiEntities.length > 0) {
          Text('医疗实体').fontSize(12).fontWeight(FontWeight.Medium).fontColor('#1E293B').margin({ top: 8 })
          Flex({ wrap: FlexWrap.Wrap }) {
            ForEach(this.aiEntities, (e: string) => {
              Text(e).fontSize(11).fontColor('#2563EB')
                .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                .backgroundColor('#EFF6FF').borderRadius(4)
                .margin({ right: 6, top: 4 })
            })
          }
        }
      }
      .width('100%').padding(12).backgroundColor('#FFFFFF').borderRadius(8)
      .margin({ left: 12, right: 12, top: 12 })

      Text('发言记录').fontSize(14).fontWeight(FontWeight.Medium).fontColor('#1E293B')
        .padding({ left: 16, top: 16 })

      if (this.speeches.length === 0) {
        EmptyState({ message: '暂无发言记录' })
      } else {
        List() {
          ForEach(this.speeches, (item: SpeechRecord) => {
            ListItem() {
              Column() {
                Row() {
                  Text(this.nameMap[item.speakerId] || `用户${item.speakerId}`)
                    .fontSize(14).fontWeight(FontWeight.Medium).fontColor('#2563EB')
                  Text(item.speechTime).fontSize(11).fontColor('#94A3B8').margin({ left: 8 })
                }
                Text(item.content).fontSize(14).fontColor('#475569').margin({ top: 4 }).maxLines(3)
              }
              .padding(12).width('100%')
              .backgroundColor('#FFFFFF').borderRadius(8)
              .margin({ top: 6, left: 12, right: 12 })
            }
          })
        }.layoutWeight(1)
      }

      if (this.qualityScore > 0) {
        Row() {
          Column() {
            Text(`${this.qualityScore}`).fontSize(28).fontWeight(FontWeight.Bold).fontColor('#2563EB')
            Text('会议质量').fontSize(11).fontColor('#475569')
          }.alignItems(HorizontalAlign.Center).layoutWeight(1)

          Column() {
            Text(`出勤 ${this.attendRate}%`).fontSize(11).fontColor('#475569')
            Text(`发言 ${this.speechCount} · 互动 ${this.interCount}`).fontSize(11).fontColor('#475569')
          }.alignItems(HorizontalAlign.Center).layoutWeight(1)
        }
        .width('100%').padding(12).backgroundColor('#FFFFFF').borderRadius(8)
        .margin({ left: 12, right: 12, top: 8 })
      }

      if (this.weeklyTrend.length > 0) {
        Column() {
          Text('周度趋势').fontSize(13).fontWeight(FontWeight.Medium).fontColor('#1E293B').margin({ bottom: 6 })
          ForEach(this.weeklyTrend, (w: Record<string, Object>) => {
            Row() {
              Text(w['meetingWeek'] as string).fontSize(12).fontColor('#475569')
              Blank()
              Text(`出勤${w['avgAttendRate']}%`).fontSize(12).fontColor('#2563EB')
              Text(`评分${w['avgQualityScore']}`).fontSize(12).fontColor('#059669').margin({ left: 8 })
            }.width('100%').padding({ top: 2, bottom: 2 })
          })
        }
        .width('100%').padding(12).backgroundColor('#FFFFFF').borderRadius(8)
        .margin({ left: 12, right: 12, top: 8, bottom: 12 })
      }
    }
    .width('100%').height('100%')
  }
}

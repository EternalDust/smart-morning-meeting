import router from '@ohos.router'
import { TokenManager } from '../common/utils/TokenManager'

@Entry
@Component
struct HomePage {
  @State currentTab: number = 0
  @State userName: string = ''
  private tabs: Array<{ title: string, icon: Resource }> = [
    { title: '签到', icon: $r('app.media.tab_sign') },
    { title: '汇报', icon: $r('app.media.tab_report') },
    { title: '互动', icon: $r('app.media.tab_interact') }
  ]

  async aboutToAppear() {
    this.userName = await TokenManager.getUserName()
  }

  async logout() {
    await TokenManager.clear()
    router.replaceUrl({ url: 'pages/LoginPage' })
  }

  @Builder TabContent(index: number) {
    if (index === 0) { SignInPageContent() }
    else if (index === 1) { ReportPageContent() }
    else { InteractionPageContent() }
  }

  build() {
    Column() {
      Row() {
        Column() {
          Text('周一科室晨会').fontSize(16).fontWeight(FontWeight.Medium).fontColor('#1E293B')
          Text(`你好，${this.userName}`).fontSize(12).fontColor('#475569')
        }
        Blank()
        Button('退出')
          .height(28).fontSize(12)
          .backgroundColor('#FEF2F2').fontColor('#DC2626').borderRadius(6)
          .onClick(() => this.logout())
      }
      .width('100%').padding({ left: 16, right: 16, top: 12, bottom: 12 })
      .backgroundColor('#FFFFFF')

      Tabs({ index: this.currentTab }) {
        TabContent() { this.TabContent(0) }.tabBar(this.TabBarBuilder(0))
        TabContent() { this.TabContent(1) }.tabBar(this.TabBarBuilder(1))
        TabContent() { this.TabContent(2) }.tabBar(this.TabBarBuilder(2))
      }
      .barPosition(BarPosition.End)
      .barHeight(56)
      .onChange((index: number) => this.currentTab = index)
      .layoutWeight(1)
    }
    .width('100%').height('100%')
    .backgroundColor('#F8FAFC')
  }

  @Builder TabBarBuilder(index: number) {
    Column() {
      Image(this.tabs[index].icon)
        .width(24).height(24)
        .fillColor(this.currentTab === index ? '#2563EB' : '#94A3B8')
      Text(this.tabs[index].title)
        .fontSize(10)
        .fontColor(this.currentTab === index ? '#2563EB' : '#94A3B8')
        .margin({ top: 2 })
    }
  }
}

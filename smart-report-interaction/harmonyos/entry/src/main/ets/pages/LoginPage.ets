import router from '@ohos.router'
import { ApiService, LoginData } from '../common/api/ApiService'
import { TokenManager } from '../common/utils/TokenManager'

@Entry
@Component
struct LoginPage {
  @State userId: string = ''
  @State password: string = ''
  @State isLoading: boolean = false
  @State errorMsg: string = ''
  @State showPassword: boolean = false

  async login() {
    if (!this.userId || !this.password) {
      this.errorMsg = '请输入工号和密码'
      return
    }
    this.isLoading = true
    this.errorMsg = ''
    try {
      const res = await ApiService.login(this.userId, this.password)
      const data = res.data as LoginData
      await TokenManager.saveToken(data.token)
      await TokenManager.saveUser(data.userId, data.userName)
      router.pushUrl({ url: 'pages/HomePage' })
    } catch (e) {
      this.errorMsg = '工号或密码错误，请重试'
    } finally {
      this.isLoading = false
    }
  }

  build() {
    Column() {
      Column() {
        Text('智慧晨会')
          .fontSize(28).fontWeight(FontWeight.Bold).fontColor('#1E293B')
        Text('数字医疗协同决策平台')
          .fontSize(14).fontColor('#475569').margin({ top: 8 })
      }
      .margin({ top: 80, bottom: 48 })

      Text('工号').fontSize(14).fontColor('#475569').margin({ bottom: 8 }).alignSelf(ItemAlign.Start).padding({ left: 32 })
      TextInput({ placeholder: '请输入工号', text: this.userId })
        .height(48).width('100%').padding({ left: 32, right: 32 })
        .backgroundColor('#F1F5F9').borderRadius(8)
        .onChange((value: string) => this.userId = value)

      Text('密码').fontSize(14).fontColor('#475569').margin({ top: 20, bottom: 8 }).alignSelf(ItemAlign.Start).padding({ left: 32 })
      Row() {
        TextInput({ placeholder: '请输入密码', text: this.password })
          .type(this.showPassword ? InputType.Normal : InputType.Password)
          .height(48).layoutWeight(1)
          .backgroundColor('#F1F5F9').borderRadius(8)
          .onChange((value: string) => this.password = value)
        Image(this.showPassword ? $r('app.media.eye_open') : $r('app.media.eye_close'))
          .width(24).height(24).margin({ left: 8 })
          .onClick(() => this.showPassword = !this.showPassword)
      }.padding({ left: 32, right: 32 })

      if (this.errorMsg) {
        Text(this.errorMsg)
          .fontSize(13).fontColor('#DC2626').margin({ top: 16 })
      }

      Button(this.isLoading ? '登录中...' : '登  录')
        .width('100%').height(48)
        .margin({ left: 32, right: 32, top: 28 })
        .enabled(!this.isLoading)
        .backgroundColor(this.isLoading ? '#93C5FD' : '#2563EB')
        .borderRadius(8)
        .onClick(() => this.login())
    }
    .width('100%').height('100%')
    .backgroundColor('#F8FAFC')
  }
}

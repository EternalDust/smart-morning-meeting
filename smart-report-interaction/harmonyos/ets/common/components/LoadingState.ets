@Component
export struct LoadingState {
  @Prop message: string = '加载中...'

  build() {
    Column() {
      LoadingProgress()
        .width(40).height(40)
        .color('#2563EB')
      Text(this.message)
        .fontSize(14).fontColor('#94A3B8').margin({ top: 12 })
    }
    .width('100%')
    .justifyContent(FlexAlign.Center)
    .padding({ top: 60, bottom: 60 })
  }
}

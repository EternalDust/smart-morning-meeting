@Component
export struct EmptyState {
  @Prop icon: string = 'document'
  @Prop message: string = '暂无数据'

  build() {
    Column() {
      Image($r('app.media.empty_icon'))
        .width(80).height(80).opacity(0.3)
      Text(this.message)
        .fontSize(14).fontColor('#94A3B8').margin({ top: 12 })
    }
    .width('100%')
    .justifyContent(FlexAlign.Center)
    .padding({ top: 60, bottom: 60 })
  }
}

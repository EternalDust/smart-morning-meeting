@Component
export struct StatCard {
  @Prop label: string = ''
  @Prop value: number = 0
  @Prop color: string = '#2563EB'

  build() {
    Column() {
      Text(this.value.toString())
        .fontSize(24).fontWeight(FontWeight.Bold).fontColor(this.color)
      Text(this.label)
        .fontSize(12).fontColor('#475569').margin({ top: 4 })
    }
    .width('22%')
    .padding(12)
    .backgroundColor('#FFFFFF')
    .borderRadius(8)
    .shadow({ radius: 4, color: 'rgba(0,0,0,0.06)' })
    .alignItems(HorizontalAlign.Center)
  }
}

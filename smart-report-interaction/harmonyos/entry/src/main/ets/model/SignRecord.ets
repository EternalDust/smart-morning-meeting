export class SignRecord {
  id: number = 0
  meetingId: number = 0
  userId: number = 0
  userName: string = ''
  signTime: string = ''
  signType: number = 0
  signStatus: number = 0
}

export class SignStats {
  shouldAttend: number = 0
  normal: number = 0
  late: number = 0
  absent: number = 0
  signed: number = 0
}

export class SpeechRecord {
  id: number = 0
  meetingId: number = 0
  speakerId: number = 0
  speakerName: string = ''
  content: string = ''
  speechTime: string = ''
  keyPoints: string = ''
}

export class MeetingSummary {
  id: number = 0
  meetingId: number = 0
  summary: string = ''
  createTime: string = ''
}

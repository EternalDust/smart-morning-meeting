export class ApiResponse<T> {
  success: boolean = false
  code: number = 0
  msg: string = ''
  data: T | null = null
}

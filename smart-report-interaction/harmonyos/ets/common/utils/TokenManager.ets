import preferences from '@ohos.data.preferences'

const STORE_NAME = 'smm_prefs'
const KEY_TOKEN = 'jwt_token'
const KEY_USER_ID = 'user_id'
const KEY_USER_NAME = 'user_name'

export class TokenManager {
  private static prefs: preferences.Preferences | null = null

  static async init(context: Context): Promise<void> {
    this.prefs = await preferences.getPreferences(context, STORE_NAME)
  }

  static async saveToken(token: string): Promise<void> {
    await this.prefs?.put(KEY_TOKEN, token)
    await this.prefs?.flush()
  }

  static async getToken(): Promise<string> {
    return await this.prefs?.get(KEY_TOKEN, '') as string ?? ''
  }

  static async saveUser(userId: string, userName: string): Promise<void> {
    await this.prefs?.put(KEY_USER_ID, userId)
    await this.prefs?.put(KEY_USER_NAME, userName)
    await this.prefs?.flush()
  }

  static async getUserId(): Promise<string> {
    return await this.prefs?.get(KEY_USER_ID, '') as string ?? ''
  }

  static async getUserName(): Promise<string> {
    return await this.prefs?.get(KEY_USER_NAME, '') as string ?? ''
  }

  static async clear(): Promise<void> {
    await this.prefs?.delete(KEY_TOKEN)
    await this.prefs?.delete(KEY_USER_ID)
    await this.prefs?.delete(KEY_USER_NAME)
    await this.prefs?.flush()
  }
}

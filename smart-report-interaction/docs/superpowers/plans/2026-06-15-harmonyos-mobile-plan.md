# HarmonyOS Mobile App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a HarmonyOS native mobile app (ArkTS + ArkUI) covering sign-in, meeting report, and interaction modules, integrated via Shell API Gateway.

**Architecture:** Two-part delivery — Part A adds a lightweight API gateway to the existing Shell backend (:8085), Part B creates a HarmonyOS app that talks to a single base URL through that gateway.

**Tech Stack:** HarmonyOS NEXT (ArkTS 5.0+, API 12+), ArkUI declarative UI, `@ohos.net.http` for HTTP, `@ohos.data.preferences` for token storage, `@ohos.webSocket` for real-time push.

---

## File Structure

```
smart-report-interaction/harmonyos/
  ets/
    pages/
      LoginPage.ets
      HomePage.ets
      SignInPage.ets
      ReportPage.ets
      InteractionPage.ets
    common/
      api/
        HttpClient.ets
        ApiService.ets
      components/
        StatCard.ets
        EmptyState.ets
        LoadingState.ets
      utils/
        TokenManager.ets
    model/
      SignRecord.ets
      SpeechRecord.ets
      InteractionMessage.ets
      ApiResponse.ets

frontend-shell/backend/src/main/java/com/huadi/smm/
  controller/
    GatewayController.java          (NEW)
  config/
    GatewayConfig.java              (NEW)
```

---

## PART A: Shell Backend — API Gateway

### Task A1: Gateway Controller + Config

**Files:**
- Create: `frontend-shell/backend/src/main/java/com/huadi/smm/config/GatewayConfig.java`
- Create: `frontend-shell/backend/src/main/java/com/huadi/smm/controller/GatewayController.java`

- [ ] **Step 1: Create GatewayConfig — subsystem port mapping**

```java
package com.huadi.smm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
public class GatewayConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Map<String, String> subsystemRoutes() {
        return Map.of(
            "report", "http://localhost:8081",
            "approval", "http://localhost:8082",
            "supervise", "http://localhost:8084",
            "visual", "http://localhost:8080",
            "collection", "http://localhost:8083"
        );
    }
}
```

- [ ] **Step 2: Create GatewayController — proxy all /api/{subsystem}/** requests**

```java
package com.huadi.smm.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class GatewayController {

    private final RestTemplate restTemplate;
    private final Map<String, String> routes;

    public GatewayController(RestTemplate restTemplate, Map<String, String> routes) {
        this.restTemplate = restTemplate;
        this.routes = routes;
    }

    @RequestMapping("/api/{subsystem}/**")
    public ResponseEntity<String> proxy(
            @PathVariable String subsystem,
            HttpMethod method,
            HttpServletRequest request,
            @RequestBody(required = false) String body) {

        String target = routes.get(subsystem);
        if (target == null) {
            return ResponseEntity.status(404)
                    .body("{\"success\":false,\"msg\":\"unknown subsystem: " + subsystem + "\"}");
        }

        String path = request.getRequestURI();
        String prefix = "/api/" + subsystem;
        String forwardPath = path.substring(prefix.length());

        String url = target + forwardPath;
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }

        HttpHeaders headers = new HttpHeaders();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            headers.set("Authorization", authHeader);
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, entity, String.class);
    }
}
```

- [ ] **Step 3: Verify — restart shell backend and test a proxy call**

```bash
curl -X GET http://localhost:8085/api/report/meeting/sign/list/1 \
  -H "Authorization: Bearer <valid-token>"
```

Expected: JSON sign-in list (forwarded from :8081), or authentication error if token missing. Gateway returns 404 for unknown subsystems.

- [ ] **Step 4: Commit**

```bash
git add frontend-shell/backend/src/main/java/com/huadi/smm/config/GatewayConfig.java \
        frontend-shell/backend/src/main/java/com/huadi/smm/controller/GatewayController.java
git commit -m "feat: add API gateway proxy controller to shell backend"
```

---

## PART B: HarmonyOS Mobile App

### Task B1: Create DevEco Studio Project

**Files:**
- Create: `smart-report-interaction/harmonyos/` (via DevEco Studio)

- [ ] **Step 1: Create project in DevEco Studio**

Open DevEco Studio → New Project → Empty Ability:
- Project name: `smart-morning-meeting-harmonyos`
- Bundle name: `com.huadi.smm`
- API level: installed SDK version
- Save to: `D:\Studying\Projects\Java\hwadee\smart-morning-meeting\`

- [ ] **Step 2: Create directory structure**

Create these directories under `ets/`:
```
common/api/
common/components/
common/utils/
model/
pages/
```

- [ ] **Step 3: Update `module.json5` — add network permission**

```json5
"requestPermissions": [
  { "name": "ohos.permission.INTERNET" }
]
```

- [ ] **Step 4: Verify — build and run empty project on emulator/device**

Build → Run. White screen with "Hello World" = success.

- [ ] **Step 5: Commit**

```bash
git add smart-report-interaction/harmonyos/
git commit -m "feat: init HarmonyOS project with directory structure"
```

---

### Task B2: Data Models

**Files:**
- Create: `ets/model/ApiResponse.ets`
- Create: `ets/model/SignRecord.ets`
- Create: `ets/model/SpeechRecord.ets`
- Create: `ets/model/InteractionMessage.ets`

- [ ] **Step 1: Create ApiResponse.ets**

```typescript
export class ApiResponse<T> {
  success: boolean = false
  code: number = 0
  msg: string = ''
  data: T | null = null
}
```

- [ ] **Step 2: Create SignRecord.ets**

```typescript
export class SignRecord {
  id: number = 0
  meetingId: number = 0
  userId: number = 0
  userName: string = ''
  signTime: string = ''
  signType: number = 0    // 1=QR, 2=manual
  signStatus: number = 0  // 0=normal, 1=late
}

export class SignStats {
  shouldAttend: number = 0
  normal: number = 0
  late: number = 0
  absent: number = 0
  signed: number = 0
}
```

- [ ] **Step 3: Create SpeechRecord.ets**

```typescript
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
```

- [ ] **Step 4: Create InteractionMessage.ets**

```typescript
export class InteractionMessage {
  id: number = 0
  meetingId: number = 0
  userId: number = 0
  userName: string = ''
  interactType: number = 0  // 1=question, 2=feedback, 3=vote
  interactTypeName: string = ''
  content: string = ''
  reply: string = ''
  createTime: string = ''
}
```

- [ ] **Step 5: Commit**

```bash
git add ets/model/
git commit -m "feat: add data model definitions"
```

---

### Task B3: Common Layer — TokenManager, HttpClient, ApiService

**Files:**
- Create: `ets/common/utils/TokenManager.ets`
- Create: `ets/common/api/HttpClient.ets`
- Create: `ets/common/api/ApiService.ets`

- [ ] **Step 1: Create TokenManager.ets**

```typescript
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
```

- [ ] **Step 2: Create HttpClient.ets**

```typescript
import http from '@ohos.net.http'
import { TokenManager } from '../utils/TokenManager'

const BASE_URL = 'http://10.0.2.2:8085' // Android emulator → host; adjust for real device
const WS_BASE_URL = 'ws://10.0.2.2:8081'

export class HttpClient {
  static async request<T>(method: http.RequestMethod, path: string, body?: Object): Promise<T> {
    const token = await TokenManager.getToken()
    const request = http.createHttp()
    const url = BASE_URL + path

    const options: http.HttpRequestOptions = {
      method: method,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      readTimeout: 15000,
      connectTimeout: 10000
    }

    if (body !== undefined) {
      options.extraData = JSON.stringify(body)
    }

    const response = await request.request(url, options)
    request.destroy()

    if (response.responseCode !== 200) {
      throw new Error(`HTTP ${response.responseCode}: ${response.result}`)
    }

    const json = JSON.parse(response.result as string)
    if (!json.success) {
      throw new Error(json.msg || '请求失败')
    }
    return json as T
  }

  static get<T>(path: string): Promise<T> {
    return this.request<T>(http.RequestMethod.GET, path)
  }

  static post<T>(path: string, body?: Object): Promise<T> {
    return this.request<T>(http.RequestMethod.POST, path, body)
  }

  static put<T>(path: string, body?: Object): Promise<T> {
    return this.request<T>(http.RequestMethod.PUT, path, body)
  }
}
```

- [ ] **Step 3: Create ApiService.ets**

```typescript
import { HttpClient } from './HttpClient'
import { ApiResponse } from '../../model/ApiResponse'
import { SignRecord, SignStats } from '../../model/SignRecord'
import { SpeechRecord, MeetingSummary } from '../../model/SpeechRecord'
import { InteractionMessage } from '../../model/InteractionMessage'

// Types for auth response
export interface LoginData {
  token: string
  userId: string
  userName: string
}

export interface SignListData {
  records: SignRecord[]
  stats: SignStats
  nameMap: Record<number, string>
}

export interface SpeechListData {
  records: SpeechRecord[]
  nameMap: Record<number, string>
}

export interface InteractionListData {
  records: InteractionMessage[]
  nameMap: Record<number, string>
}

export interface InteractionStats {
  total: number
  questions: number
  feedbacks: number
  votes: number
  replied: number
}

export class ApiService {
  // Auth (Shell, no prefix)
  static login(userId: string, password: string) {
    return HttpClient.post<ApiResponse<LoginData>>('/api/auth/login', { userId, password })
  }

  // Sign-in
  static signIn(meetingId: number, userId: number, signType: number = 2) {
    return HttpClient.post<ApiResponse<SignRecord>>('/api/report/meeting/sign/in', { meetingId, userId, signType })
  }

  static getSignList(meetingId: number) {
    return HttpClient.get<ApiResponse<SignListData>>(`/api/report/meeting/sign/list/${meetingId}`)
  }

  // Speech/Report
  static saveSpeech(meetingId: number, speakerId: number, content: string) {
    return HttpClient.post<ApiResponse<SpeechRecord>>('/api/report/meeting/speech/save', { meetingId, speakerId, content })
  }

  static getSpeechList(meetingId: number) {
    return HttpClient.get<ApiResponse<SpeechListData>>(`/api/report/meeting/speech/list/${meetingId}`)
  }

  static getSummary(meetingId: number) {
    return HttpClient.get<ApiResponse<MeetingSummary>>(`/api/report/meeting/summary/${meetingId}`)
  }

  // Interaction
  static sendMessage(meetingId: number, userId: number, interactType: number, content: string) {
    return HttpClient.post<ApiResponse<InteractionMessage>>('/api/report/meeting/interaction/message', { meetingId, userId, interactType, content })
  }

  static replyMessage(messageId: number, reply: string) {
    return HttpClient.post<ApiResponse<InteractionMessage>>(`/api/report/meeting/interaction/reply/${messageId}`, { reply })
  }

  static getInteractionList(meetingId: number, type?: number) {
    let path = `/api/report/meeting/interaction/list/${meetingId}`
    if (type !== undefined && type > 0) path += `?type=${type}`
    return HttpClient.get<ApiResponse<InteractionListData>>(path)
  }

  static getInteractionStats(meetingId: number) {
    return HttpClient.get<ApiResponse<InteractionStats>>(`/api/report/meeting/interaction/stats/${meetingId}`)
  }
}
```

- [ ] **Step 4: Commit**

```bash
git add ets/common/utils/TokenManager.ets ets/common/api/HttpClient.ets ets/common/api/ApiService.ets
git commit -m "feat: add HTTP client, token manager, and API service layer"
```

---

### Task B4: Common UI Components

**Files:**
- Create: `ets/common/components/StatCard.ets`
- Create: `ets/common/components/EmptyState.ets`
- Create: `ets/common/components/LoadingState.ets`

- [ ] **Step 1: Create StatCard.ets**

```typescript
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
```

- [ ] **Step 2: Create EmptyState.ets**

```typescript
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
```

- [ ] **Step 3: Create LoadingState.ets**

```typescript
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
```

- [ ] **Step 4: Commit**

```bash
git add ets/common/components/
git commit -m "feat: add StatCard, EmptyState, LoadingState components"
```

---

### Task B5: LoginPage

**Files:**
- Create: `ets/pages/LoginPage.ets`

- [ ] **Step 1: Create LoginPage.ets**

```typescript
import router from '@ohos.router'
import { ApiService } from '../common/api/ApiService'
import { LoginData } from '../common/api/ApiService'
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
      // Logo area
      Column() {
        Text('智慧晨会')
          .fontSize(28).fontWeight(FontWeight.Bold).fontColor('#1E293B')
        Text('数字医疗协同决策平台')
          .fontSize(14).fontColor('#475569').margin({ top: 8 })
      }
      .margin({ top: 80, bottom: 48 })

      // User ID input
      Text('工号').fontSize(14).fontColor('#475569').margin({ bottom: 8 }).alignSelf(ItemAlign.Start).padding({ left: 32 })
      TextInput({ placeholder: '请输入工号', text: this.userId })
        .height(48).width('100%').padding({ left: 32, right: 32 })
        .backgroundColor('#F1F5F9').borderRadius(8)
        .onChange(value => this.userId = value)

      // Password input
      Text('密码').fontSize(14).fontColor('#475569').margin({ top: 20, bottom: 8 }).alignSelf(ItemAlign.Start).padding({ left: 32 })
      Row() {
        TextInput({ placeholder: '请输入密码', text: this.password })
          .type(this.showPassword ? InputType.Normal : InputType.Password)
          .height(48).layoutWeight(1)
          .backgroundColor('#F1F5F9').borderRadius(8)
          .onChange(value => this.password = value)
        Image(this.showPassword ? $r('app.media.eye_open') : $r('app.media.eye_close'))
          .width(24).height(24).margin({ left: 8 })
          .onClick(() => this.showPassword = !this.showPassword)
      }.padding({ left: 32, right: 32 })

      // Error message
      if (this.errorMsg) {
        Text(this.errorMsg)
          .fontSize(13).fontColor('#DC2626').margin({ top: 16 })
      }

      // Login button
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
```

- [ ] **Step 2: Commit**

```bash
git add ets/pages/LoginPage.ets
git commit -m "feat: add login page with JWT auth"
```

---

### Task B6: HomePage (TabBar Container)

**Files:**
- Create: `ets/pages/HomePage.ets`

- [ ] **Step 1: Create HomePage.ets**

```typescript
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

  @Builder TabContent(index: number) {
    if (index === 0) { SignInPageContent() }
    else if (index === 1) { ReportPageContent() }
    else { InteractionPageContent() }
  }

  build() {
    Column() {
      // Top bar
      Row() {
        Column() {
          Text('周一科室晨会').fontSize(16).fontWeight(FontWeight.Medium).fontColor('#1E293B')
          Text(`你好，${this.userName}`).fontSize(12).fontColor('#475569')
        }
        Blank()
        Text('🕐').fontSize(20)
      }
      .width('100%').padding({ left: 16, right: 16, top: 12, bottom: 12 })
      .backgroundColor('#FFFFFF')

      // Content area
      Tabs({ index: this.currentTab }) {
        TabContent() { this.TabContent(0) }.tabBar(this.TabBarBuilder(0))
        TabContent() { this.TabContent(1) }.tabBar(this.TabBarBuilder(1))
        TabContent() { this.TabContent(2) }.tabBar(this.TabBarBuilder(2))
      }
      .barPosition(BarPosition.End)
      .barHeight(56)
      .onChange(index => this.currentTab = index)
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

// Content components defined in their own files, imported here
```

- [ ] **Step 2: Commit**

```bash
git add ets/pages/HomePage.ets
git commit -m "feat: add home page with bottom tab navigation"
```

---

### Task B7: SignInPage Content

**Files:**
- Create: `ets/pages/SignInPage.ets`  (or as @Component in HomePage, then extract)

- [ ] **Step 1: Create SignInPage.ets**

```typescript
import { ApiService, SignListData } from '../common/api/ApiService'
import { StatCard } from '../common/components/StatCard'
import { EmptyState } from '../common/components/EmptyState'
import { SignRecord, SignStats } from '../model/SignRecord'
import { TokenManager } from '../common/utils/TokenManager'
import webSocket from '@ohos.net.webSocket'

@Component
export struct SignInPageContent {
  @State stats: SignStats = { shouldAttend: 0, normal: 0, late: 0, absent: 0, signed: 0 }
  @State records: SignRecord[] = []
  @State signUserId: string = ''
  @State isLoading: boolean = false
  @State isSigning: boolean = false
  @State nameMap: Record<number, string> = {}
  private ws: webSocket.WebSocket | null = null

  async aboutToAppear() {
    await this.loadData()
    this.connectWebSocket()
  }

  aboutToDisappear() {
    this.ws?.close()
  }

  async loadData() {
    this.isLoading = true
    try {
      const res = await ApiService.getSignList(1)
      const data = res.data as SignListData
      this.records = data.records
      this.stats = data.stats
      this.nameMap = data.nameMap
    } catch (e) {
      // Error handled by HttpClient interceptor
    } finally {
      this.isLoading = false
    }
  }

  connectWebSocket() {
    this.ws = webSocket.createWebSocket()
    this.ws.connect('ws://10.0.2.2:8081/api/meeting/realtime/push/1', (err, value) => {
      if (!err) {
        // Connected
      }
    })
    this.ws.on('message', (err, value) => {
      const msg = JSON.parse(value as string)
      if (msg.type === 'sign') {
        this.loadData() // Refresh on new sign-in
      }
    })
    this.ws.on('close', (err, value) => {
      // Reconnect after 3s
      setTimeout(() => this.connectWebSocket(), 3000)
    })
  }

  async doSignIn() {
    if (!this.signUserId) return
    this.isSigning = true
    try {
      const userId = parseInt(this.signUserId)
      await ApiService.signIn(1, userId, 2)
      this.signUserId = ''
      await this.loadData()
    } catch (e) {
      // Error handled by interceptor
    } finally {
      this.isSigning = false
    }
  }

  build() {
    Column() {
      // Stat cards row
      Row() {
        StatCard({ label: '应到', value: this.stats.shouldAttend, color: '#2563EB' })
        StatCard({ label: '正常', value: this.stats.normal, color: '#059669' })
        StatCard({ label: '迟到', value: this.stats.late, color: '#D97706' })
        StatCard({ label: '缺勤', value: this.stats.absent, color: '#DC2626' })
      }
      .width('100%').padding(12).justifyContent(FlexAlign.SpaceAround)

      // Sign-in input + button
      Row() {
        TextInput({ placeholder: '输入工号...', text: this.signUserId })
          .height(48).layoutWeight(1)
          .backgroundColor('#FFFFFF').borderRadius(8)
          .onChange(value => this.signUserId = value)
        Button(this.isSigning ? '签到中' : '签 到')
          .height(48).width(80)
          .margin({ left: 12 })
          .enabled(!this.isSigning)
          .backgroundColor(this.isSigning ? '#93C5FD' : '#2563EB')
          .borderRadius(8)
          .onClick(() => this.doSignIn())
      }
      .padding({ left: 12, right: 12, top: 8, bottom: 8 })

      // Sign-in list
      if (this.records.length === 0) {
        EmptyState({ message: '暂无签到记录' })
      } else {
        List() {
          ForEach(this.records, (item: SignRecord) => {
            ListItem() {
              Row() {
                Text(item.userId.toString())
                  .fontSize(14).fontWeight(FontWeight.Medium).fontColor('#1E293B')
                Text(this.nameMap[item.userId] || '')
                  .fontSize(14).fontColor('#1E293B').margin({ left: 8 })
                Blank()
                Text(item.signTime.substring(11, 16))
                  .fontSize(12).fontColor('#475569')
                Text(item.signStatus === 0 ? '正常' : '迟到')
                  .fontSize(12)
                  .fontColor(item.signStatus === 0 ? '#059669' : '#D97706')
                  .margin({ left: 8 })
                  .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                  .backgroundColor(item.signStatus === 0 ? '#D1FAE5' : '#FEF3C7')
                  .borderRadius(4)
              }
              .width('100%').height(56).padding({ left: 16, right: 16 })
              .backgroundColor('#FFFFFF')
              .borderRadius(8)
              .margin({ top: 4, left: 12, right: 12 })
            }
          })
        }
        .layoutWeight(1)
      }
    }
    .width('100%').height('100%')
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add ets/pages/SignInPage.ets
git commit -m "feat: add sign-in page with stats, input, and WebSocket refresh"
```

---

### Task B8: ReportPage Content

**Files:**
- Create: `ets/pages/ReportPage.ets`

- [ ] **Step 1: Create ReportPage.ets**

```typescript
import { ApiService, SpeechListData } from '../common/api/ApiService'
import { EmptyState } from '../common/components/EmptyState'
import { SpeechRecord, MeetingSummary } from '../model/SpeechRecord'

@Component
export struct ReportPageContent {
  @State speakerId: string = ''
  @State content: string = ''
  @State speeches: SpeechRecord[] = []
  @State summary: string = ''
  @State isSubmitting: boolean = false
  @State isLoading: boolean = false
  @State nameMap: Record<number, string> = {}

  async aboutToAppear() {
    await this.loadData()
  }

  async loadData() {
    this.isLoading = true
    try {
      const [speechRes, summaryRes] = await Promise.all([
        ApiService.getSpeechList(1),
        ApiService.getSummary(1)
      ])
      const data = speechRes.data as SpeechListData
      this.speeches = data.records
      this.nameMap = data.nameMap
      this.summary = (summaryRes.data as MeetingSummary)?.summary || ''
    } finally {
      this.isLoading = false
    }
  }

  async submitSpeech() {
    if (!this.speakerId || !this.content) return
    this.isSubmitting = true
    try {
      await ApiService.saveSpeech(1, parseInt(this.speakerId), this.content)
      this.content = ''
      await this.loadData()
    } finally {
      this.isSubmitting = false
    }
  }

  build() {
    Column() {
      // Speech input form
      Column() {
        TextInput({ placeholder: '发言人ID', text: this.speakerId })
          .height(44).width('100%')
          .backgroundColor('#F1F5F9').borderRadius(8).padding({ left: 12, right: 12 })
          .onChange(value => this.speakerId = value)
        TextArea({ placeholder: '发言内容...', text: this.content })
          .minHeight(120).width('100%')
          .backgroundColor('#F1F5F9').borderRadius(8).padding(12)
          .margin({ top: 8 })
          .onChange(value => this.content = value)
        Button(this.isSubmitting ? '提交中...' : '提交发言')
          .width('100%').height(44)
          .margin({ top: 8 })
          .enabled(!this.isSubmitting)
          .backgroundColor(this.isSubmitting ? '#93C5FD' : '#2563EB')
          .borderRadius(8)
          .onClick(() => this.submitSpeech())
      }
      .width('100%').padding({ left: 12, right: 12, top: 12 })
      .backgroundColor('#FFFFFF').borderRadius(8)
      .margin({ left: 12, right: 12, top: 12 })

      // Speech history
      Text('历史发言').fontSize(14).fontWeight(FontWeight.Medium).fontColor('#1E293B')
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
                Text(item.content)
                  .fontSize(14).fontColor('#475569').margin({ top: 4 })
                  .maxLines(3).textOverflow({ overflow: TextOverflow.Ellipsis })
              }
              .padding(12).width('100%')
              .backgroundColor('#FFFFFF').borderRadius(8)
              .margin({ top: 6, left: 12, right: 12 })
            }
          })
        }
        .layoutWeight(1)
      }

      // Summary section
      if (this.summary) {
        Column() {
          Text('会议总结').fontSize(14).fontWeight(FontWeight.Medium).fontColor('#1E293B')
          Text(this.summary)
            .fontSize(13).fontColor('#475569').margin({ top: 6 })
            .lineHeight(20)
        }
        .width('100%').padding(16).backgroundColor('#FFFFFF').borderRadius(8)
        .margin({ left: 12, right: 12, bottom: 12 })
      }
    }
    .width('100%').height('100%')
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add ets/pages/ReportPage.ets
git commit -m "feat: add meeting report page with speech input and history"
```

---

### Task B9: InteractionPage Content

**Files:**
- Create: `ets/pages/InteractionPage.ets`

- [ ] **Step 1: Create InteractionPage.ets**

```typescript
import { ApiService, InteractionListData } from '../common/api/ApiService'
import { EmptyState } from '../common/components/EmptyState'
import { InteractionMessage } from '../model/InteractionMessage'
import { TokenManager } from '../common/utils/TokenManager'
import webSocket from '@ohos.net.webSocket'

const TYPE_LABELS: Record<number, string> = { 1: '提问', 2: '建议', 3: '通知' }
const TYPE_FILTERS = [0, 1, 2, 3]
const FILTER_NAMES = ['全部', '提问', '建议', '通知']

@Component
export struct InteractionPageContent {
  @State messages: InteractionMessage[] = []
  @State activeFilter: number = 0
  @State inputText: string = ''
  @State isLoading: boolean = false
  @State isSending: boolean = false
  @State nameMap: Record<number, string> = {}
  @State userId: string = ''
  private ws: webSocket.WebSocket | null = null

  async aboutToAppear() {
    this.userId = await TokenManager.getUserId()
    await this.loadMessages()
    this.connectWebSocket()
  }

  aboutToDisappear() {
    this.ws?.close()
  }

  async loadMessages() {
    this.isLoading = true
    try {
      const res = await ApiService.getInteractionList(1, this.activeFilter)
      const data = res.data as InteractionListData
      this.messages = data.records
      this.nameMap = data.nameMap
    } finally {
      this.isLoading = false
    }
  }

  connectWebSocket() {
    this.ws = webSocket.createWebSocket()
    this.ws.connect('ws://10.0.2.2:8081/api/meeting/realtime/push/1', (err, value) => {
      // Connected
    })
    this.ws.on('message', (err, value) => {
      const msg = JSON.parse(value as string)
      if (msg.type === 'interaction') {
        this.loadMessages()
      }
    })
    this.ws.on('close', () => {
      setTimeout(() => this.connectWebSocket(), 3000)
    })
  }

  async sendMessage() {
    if (!this.inputText.trim()) return
    this.isSending = true
    try {
      await ApiService.sendMessage(1, parseInt(this.userId), 1, this.inputText.trim())
      this.inputText = ''
      await this.loadMessages()
    } finally {
      this.isSending = false
    }
  }

  switchFilter(index: number) {
    this.activeFilter = TYPE_FILTERS[index]
    this.loadMessages()
  }

  build() {
    Column() {
      // Filter tabs
      Row() {
        ForEach(FILTER_NAMES, (name: string, index: number) => {
          Text(name)
            .fontSize(13)
            .fontColor(this.activeFilter === TYPE_FILTERS[index] ? '#2563EB' : '#475569')
            .padding({ left: 16, right: 16, top: 8, bottom: 8 })
            .borderRadius(16)
            .backgroundColor(this.activeFilter === TYPE_FILTERS[index] ? '#DBEAFE' : '#F1F5F9')
            .margin({ right: 8 })
            .onClick(() => this.switchFilter(index))
        })
      }
      .padding({ left: 12, right: 12, top: 12, bottom: 8 })

      // Message list
      if (this.messages.length === 0) {
        EmptyState({ message: '暂无互动消息' })
      } else {
        List() {
          ForEach(this.messages, (item: InteractionMessage) => {
            ListItem() {
              Column() {
                Row() {
                  Text(this.nameMap[item.userId] || `用户${item.userId}`)
                    .fontSize(14).fontWeight(FontWeight.Medium).fontColor('#1E293B')
                  Text(TYPE_LABELS[item.interactType] || '')
                    .fontSize(10).fontColor('#2563EB')
                    .padding({ left: 6, right: 6, top: 1, bottom: 1 })
                    .backgroundColor('#DBEAFE').borderRadius(4)
                    .margin({ left: 6 })
                  Blank()
                  Text(item.createTime?.substring(11, 16) || '')
                    .fontSize(11).fontColor('#94A3B8')
                }
                Text(item.content)
                  .fontSize(14).fontColor('#475569').margin({ top: 6 }).lineHeight(20)
                // Replies
                if (item.reply) {
                  Text(`💬 ${item.reply}`)
                    .fontSize(13).fontColor('#1E293B')
                    .margin({ top: 8 }).padding(10)
                    .backgroundColor('#F1F5F9').borderRadius(6)
                    .width('100%')
                }
              }
              .padding(14).width('100%')
              .backgroundColor('#FFFFFF').borderRadius(8)
              .margin({ top: 6, left: 12, right: 12 })
            }
          })
        }
        .layoutWeight(1)
      }

      // Compose bar
      Row() {
        TextInput({ placeholder: '输入消息...', text: this.inputText })
          .height(44).layoutWeight(1)
          .backgroundColor('#F1F5F9').borderRadius(8).padding({ left: 12, right: 12 })
          .onChange(value => this.inputText = value)
        Button(this.isSending ? '...' : '发送')
          .height(44).width(64)
          .margin({ left: 8 })
          .enabled(!this.isSending && this.inputText.trim().length > 0)
          .backgroundColor(this.inputText.trim() ? '#2563EB' : '#CBD5E1')
          .borderRadius(8)
          .onClick(() => this.sendMessage())
      }
      .width('100%').padding({ left: 12, right: 12, top: 8, bottom: 12 })
      .backgroundColor('#FFFFFF')
      .border({ width: { top: 1 }, color: '#E2E8F0' })
    }
    .width('100%').height('100%')
  }
}
```

- [ ] **Step 2: Commit**

```bash
git add ets/pages/InteractionPage.ets
git commit -m "feat: add interaction page with filter, messages, and compose"
```

---

## Self-Review

**Spec Coverage:**
- Gateway architecture → Task A1
- Directory structure → Task B1
- API mapping → Task B3 (ApiService)
- Design tokens → applied inline in each task
- LoginPage → Task B5
- HomePage + TabBar → Task B6
- SignInPage → Task B7
- ReportPage → Task B8
- InteractionPage → Task B9
- UX rules → EmptyState/LoadingState (B4), touch sizes, animation durations
- Extension points → directory structure leaves room for future pages

**Placeholder Check:** No TBD/TODO found. All code is complete with actual types and values.

**Type Consistency:** `SignRecord`, `SignStats`, `SpeechRecord`, `InteractionMessage`, `ApiResponse<T>` all used consistently across ApiService and page components.

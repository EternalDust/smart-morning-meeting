# HarmonyOS Mobile Design Spec

## Overview

- **Platform**: HarmonyOS NEXT (ArkTS + ArkUI)
- **Backend**: Existing 5-subsystem architecture, accessed via Shell API Gateway (:8085)
- **Scope**: Sign-in + Meeting Report + Interaction (3 modules from smart-report-interaction)
- **Aligns with**: Mid-term report §2.2 item 5 (Mobile Cross-Device Adaptation Module)

## Architecture

```
HarmonyOS App (ArkTS)
  │  baseURL: http://shell:8085
  ▼
Shell Backend :8085 (API Gateway)
  │  Auth: POST /api/auth/login → JWT
  │  Route: /api/report/** → strip prefix → localhost:8081
  │  Route: /api/approval/** → strip prefix → localhost:8082
  │  Route: /api/supervise/** → strip prefix → localhost:8084
  │  Route: /api/visual/** → strip prefix → localhost:8080
  │  Route: /api/collection/** → strip prefix → localhost:8083
  ▼
Subsystem Backends (unchanged)
```

- Gateway uses RestTemplate with path rewriting (lightweight, not Spring Cloud Gateway)
- WebSocket: direct connect to :8081 (bypass gateway for simplicity)
- JWT: Shell issues token, gateway transparently forwards to subsystems for validation

## Directory Structure

```
smart-morning-meeting-harmonyos/
  ets/
    pages/
      LoginPage.ets
      HomePage.ets
      SignInPage.ets
      ReportPage.ets
      InteractionPage.ets
    common/
      api/
        HttpClient.ets          # Axios-like HTTP with JWT interceptor
        ApiService.ets          # Endpoint definitions
      components/
        StatCard.ets
        EmptyState.ets
        LoadingState.ets
      utils/
        TokenManager.ets        # Preference persistence
    model/
      SignRecord.ets
      SpeechRecord.ets
      InteractionMessage.ets
      MeetingInfo.ets
```

Directory structured to allow future subsystem modules (approval/, supervise/, visual/, collection/) to be added as sibling directories under `pages/`.

## API Mapping

Mobile calls prefix `{subsystem}` → gateway strips `/api/{subsystem}` → forwards to actual backend path:

| Mobile Call | Gateway Forwards To | Backend |
|-------------|---------------------|---------|
| POST /api/report/meeting/sign/in | POST /api/meeting/sign/in | :8081 |
| GET /api/report/meeting/sign/list/{id} | GET /api/meeting/sign/list/{id} | :8081 |
| POST /api/report/meeting/speech/save | POST /api/meeting/speech/save | :8081 |
| GET /api/report/meeting/speech/list/{id} | GET /api/meeting/speech/list/{id} | :8081 |
| GET /api/report/meeting/summary/{id} | GET /api/meeting/summary/{id} | :8081 |
| POST /api/report/meeting/interaction/message | POST /api/meeting/interaction/message | :8081 |
| POST /api/report/meeting/interaction/reply/{id} | POST /api/meeting/interaction/reply/{id} | :8081 |
| GET /api/report/meeting/interaction/list/{id} | GET /api/meeting/interaction/list/{id} | :8081 |

Auth: POST /api/auth/login (Shell :8085, no forwarding)
WebSocket: ws://host:8081/api/meeting/realtime/push/{meetingId} (direct)

## Design Tokens

Primary: #2563EB | Text: #1E293B / #475569 | BG: #F8FAFC / #FFFFFF
Success: #059669 | Warning: #D97706 | Error: #DC2626
Spacing: 8vp grid (8/12/16/24/32/48)
Radius: cards 8vp / buttons 6vp
Shadow: 0 1px 3px rgba(0,0,0,.08)
Font scale: 12/14/16/18/20/24 | line-height: body 1.5 / heading 1.3

## Pages

### 1. LoginPage
- User ID + password inputs (height 48vp, label above, not placeholder-only)
- Password toggle (show/hide)
- Login button: loading state (spinner + "登录中..." + disabled)
- Error: inline message below button ("工号或密码错误") — no alert dialog
- On success: store JWT via Preference, navigate to HomePage

### 2. HomePage
- Top bar: meeting title + user name
- Content: Tab content area (Switcher or Tabs component)
- Bottom TabBar (56vp height): 签到 | 汇报 | 互动 — each icon+label
- Active tab highlighted with primary color
- 2 slots reserved for future subsystem tabs

### 3. SignInPage
- Top: 4 stat cards in a row (应到/正常/迟到/缺勤)
- Input row: user ID TextInput + "签到" Button (56vp, full-width)
- Button press: loading → success (green + checkmark 300ms) → restore
- Below: scrollable sign-in list (56vp row height, name + time + status badge)
- Empty state: "暂无签到记录" + icon
- WebSocket: receives sign events → refresh list without flicker

### 4. ReportPage
- Top: speaker picker (dropdown from attendee list) + content TextArea (min 120vp)
- Submit button: bottom-fixed, loading state on submit
- Middle: speech history list (expandable items)
- Bottom card: meeting summary (scrollable)
- Empty: "暂无发言记录" for history, "暂无会议总结" for summary

### 5. InteractionPage
- Filter tabs: 全部 | 提问 | 建议 | 通知
- Message stream: card list (avatar + name + type badge + time + content + inline replies)
- New messages fade in (150ms ease-out, WebSocket push)
- Compose bar: bottom-fixed, auto-grow TextArea (max 4 lines), send button
- Send button disabled when empty (gray), enabled when content (primary)
- Empty: "暂无互动消息"

## UX Rules (from UI/UX Pro Max)

- Touch targets ≥48vp, spacing between ≥12vp
- Animation 150-300ms, ease-out enter / ease-in exit
- Loading: skeleton for lists, spinner for buttons
- Error: inline message + retry button, no alert dialogs
- Empty: icon + text for every list/section
- Color not the only differentiator (status paired with icons)
- Safe areas: top bar + bottom tab avoid notch/gesture zone

## Gateway Implementation

Add `GatewayController` to Shell backend:
- Single entry: `@RequestMapping("/api/{subsystem}/**")`
- Extract `{subsystem}`, lookup port from config map
- `RestTemplate.exchange()` forward with same method, headers, body
- Strip `/api/{subsystem}` prefix before forwarding

Minimal change to shell backend (1 new class), zero change to subsystem backends.

## Extension Points

- `pages/` directory: add approval/, supervise/, visual/, collection/ for future subsystems
- TabBar: 2 reserved slots (current 3 items, max 5)
- `common/api/ApiService.ets`: add new endpoint groups
- Gateway: add route entry in config map (no code change)

# 五子系统整合 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 一台电脑部署完整项目，统一登录入口 + 五子系统独立运行，局域网可访问。

**Architecture:** 新建 frontend-shell（Vue3 + ElementPlus + SpringBoot 8085）做统一登录和导航，五个子系统前端通过新标签页跳转，后端各跑各的端口。合并 SQL 脚本含演示数据。

**Tech Stack:** Vue3, ElementPlus, SpringBoot 2.7.6, MyBatis-Plus, MySQL 8.0, JWT

---

### Task 1: 统一登录后端

**Files:**
- Create: `frontend-shell/backend/pom.xml`
- Create: `frontend-shell/backend/src/main/java/com/huadi/smm/ShellApplication.java`
- Create: `frontend-shell/backend/src/main/java/com/huadi/smm/controller/AuthController.java`
- Create: `frontend-shell/backend/src/main/resources/application.yml`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.6</version>
    </parent>
    <groupId>com.huadi.smm</groupId>
    <artifactId>frontend-shell</artifactId>
    <version>1.0.0</version>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.5</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
            <version>0.9.1</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
package com.huadi.smm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.huadi.smm.dao")
public class ShellApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShellApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8085

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_meeting?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 1234
    driver-class-name: com.mysql.cj.jdbc.Driver
```

- [ ] **Step 4: 创建 Entity 和 Mapper**

```java
package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_gm_members")
public class Member {
    private Long id;
    private String userId;
    private String name;
    private String password;
    private Integer role;
    private String dept;
}
```

```java
package com.huadi.smm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
```

- [ ] **Step 5: 创建 AuthController**

```java
package com.huadi.smm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.dao.MemberMapper;
import com.huadi.smm.entity.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SECRET = "smart-morning-meeting-2026";
    private static final long EXPIRATION = 86400000;

    @Resource
    private MemberMapper memberMapper;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.getOrDefault("password", "");

        LambdaQueryWrapper<Member> qw = new LambdaQueryWrapper<>();
        qw.eq(Member::getUserId, userId);
        Member member = memberMapper.selectOne(qw);

        Map<String, Object> result = new HashMap<>();
        if (member == null || !password.equals(member.getPassword())) {
            result.put("success", false);
            result.put("code", 401);
            result.put("msg", "工号或密码错误");
            return result;
        }

        String token = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();

        result.put("success", true);
        result.put("code", 200);
        result.put("msg", "success");
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("userName", member.getName());
        data.put("userId", userId);
        result.put("data", data);
        return result;
    }
}
```

- [ ] **Step 6: 编译验证**

Run: `cd frontend-shell/backend && ../../smart-report-interaction/backend/mvnw compile`
Expected: BUILD SUCCESS

---

### Task 2: 统一登录前端

**Files:**
- Create: `frontend-shell/frontend/package.json`
- Create: `frontend-shell/frontend/vite.config.js`
- Create: `frontend-shell/frontend/index.html`
- Create: `frontend-shell/frontend/src/main.js`
- Create: `frontend-shell/frontend/src/App.vue`
- Create: `frontend-shell/frontend/src/views/Login.vue`
- Create: `frontend-shell/frontend/src/views/Home.vue`
- Create: `frontend-shell/frontend/src/router/index.js`

- [ ] **Step 1: 初始化 Vue 项目**

```bash
cd frontend-shell/frontend
npm create vite@latest . -- --template vue
npm install vue-router@4 axios element-plus @element-plus/icons-vue
```

- [ ] **Step 2: vite.config.js**

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5000,
    proxy: {
      '/api': { target: 'http://localhost:8085', changeOrigin: true }
    }
  }
})
```

- [ ] **Step 3: router/index.js**

```js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/home', name: 'Home', component: () => import('../views/Home.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
```

- [ ] **Step 4: main.js**

```js
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

createApp(App).use(router).use(ElementPlus).mount('#app')
```

- [ ] **Step 5: App.vue**

```vue
<template><router-view /></template>
<style>
* { margin:0; padding:0; box-sizing:border-box }
body { font-family:'Microsoft YaHei',sans-serif; background:#F8FAFC }
</style>
```

- [ ] **Step 6: Login.vue**

```vue
<template>
  <div class="login-page">
    <div class="login-card">
      <h1>数字医疗智慧晨会平台</h1>
      <p class="subtitle">协同与决策支撑</p>
      <el-form @submit.prevent="doLogin">
        <el-input v-model="userId" placeholder="工号" size="large" style="margin-bottom:12px" />
        <el-input v-model="password" type="password" placeholder="密码" size="large" show-password style="margin-bottom:20px" />
        <el-button type="primary" size="large" @click="doLogin" :loading="loading" style="width:100%">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userId = ref('')
const password = ref('')
const loading = ref(false)

const doLogin = async () => {
  if (!userId.value) { ElMessage.warning('请输入工号'); return }
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', { userId: userId.value, password: password.value })
    if (res.data.success) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('userName', res.data.data.userName)
      router.push('/home')
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch (e) {
    ElMessage.error('登录失败')
  }
  loading.value = false
}
</script>

<style scoped>
.login-page { height:100vh; display:flex; align-items:center; justify-content:center; background:linear-gradient(135deg, #1e3a5f 0%, #2563EB 100%) }
.login-card { background:#fff; padding:40px; border-radius:12px; width:380px; box-shadow:0 4px 20px rgba(0,0,0,.15) }
.login-card h1 { font-size:20px; text-align:center; color:#1E293B; margin-bottom:4px }
.subtitle { text-align:center; color:#64748B; font-size:13px; margin-bottom:28px }
</style>
```

- [ ] **Step 7: Home.vue**

```vue
<template>
  <div class="home">
    <div class="topbar">
      <span class="greeting">{{ userName }}，欢迎</span>
      <span class="platform">数字医疗智慧晨会协同与决策支撑平台</span>
      <el-button link @click="logout" style="color:#fff">退出</el-button>
    </div>
    <div class="cards">
      <div class="card" v-for="sys in systems" :key="sys.name" @click="open(sys.url)">
        <div class="card-icon" :style="{background:sys.color}">
          <el-icon :size="28"><component :is="sys.icon" /></el-icon>
        </div>
        <div class="card-title">{{ sys.name }}</div>
        <div class="card-desc">{{ sys.desc }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, ChatDotRound, Document, DataBoard, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const userName = ref('')

const systems = [
  { name:'大数据可视化大屏', desc:'晨会数据实时监控与分析决策', url:'http://localhost:5173', color:'#2563EB', icon: Monitor },
  { name:'晨会汇报与实时交互', desc:'签到 · 汇报 · 提问 · 投票', url:'http://localhost:5174', color:'#059669', icon: ChatDotRound },
  { name:'晨会审批与议程管理', desc:'发起晨会 · 拟定议程 · 材料审核 · 流程审批', url:'http://localhost:5175', color:'#7C3AED', icon: Document },
  { name:'多源数据采集与治理', desc:'数据接入 · 清洗 · 标签化 · 溯源', url:'http://localhost:5176', color:'#D97706', icon: DataBoard },
  { name:'问题督办与闭环管理', desc:'问题登记 · 分派 · 进度跟踪 · 结案', url:'http://localhost:5177', color:'#DC2626', icon: Warning },
]

const open = (url) => window.open(url, '_blank')

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

onMounted(() => {
  userName.value = localStorage.getItem('userName') || ''
  if (!localStorage.getItem('token')) router.push('/login')
})
</script>

<style scoped>
.home { min-height:100vh; background:#F1F5F9 }
.topbar { display:flex; align-items:center; padding:0 24px; height:56px; background:#1E293B; color:#fff }
.greeting { font-size:14px }
.platform { flex:1; text-align:center; font-size:15px; font-weight:500 }
.cards { display:flex; justify-content:center; gap:20px; padding:60px 24px; flex-wrap:wrap }
.card { width:200px; padding:28px 20px; background:#fff; border-radius:10px; text-align:center; cursor:pointer; transition: all .2s; box-shadow:0 1px 3px rgba(0,0,0,.06) }
.card:hover { transform:translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,.1) }
.card-icon { width:56px; height:56px; border-radius:12px; display:flex; align-items:center; justify-content:center; margin:0 auto 14px }
.card-icon :deep(.el-icon) { color:#fff }
.card-title { font-size:14px; font-weight:600; margin-bottom:6px; color:#1E293B }
.card-desc { font-size:12px; color:#64748B; line-height:1.5 }
</style>
```

---

### Task 3: 合并数据库脚本

**Files:**
- Create: `sql/init-all.sql`

- [ ] **Step 1: 编写 init-all.sql**

合并五个子系统的建表语句，去重共享表，加演示数据。完整脚本从各子系统 `sql/init.sql` 中提取表结构，保留所有业务表，共享表（`sm_gm_members`、`sm_meeting_info`、`meeting_attendee`、`meeting_agenda`）只定义一次。加一场完整晨会的演示数据。

具体做法：读取五个 `sql/init.sql` → 提取所有 CREATE TABLE → 去重 → 加 INSERT 演示数据 → 写入 `sql/init-all.sql`。

---

### Task 4: 给 sm_gm_members 加密码字段

**Files:**
- Modify: `smart-report-interaction/sql/init.sql`
- Modify: `smart-meeting-supervise/sql/init.sql`

- [ ] **Step 1: 在共享人员表中加 password 列**

当前 `sm_gm_members` 没有密码字段。登录需要密码。

```sql
ALTER TABLE sm_gm_members ADD COLUMN password VARCHAR(64) DEFAULT '123456';
UPDATE sm_gm_members SET password = '123456';
```

更新两个引用该表的 `init.sql`，在 CREATE TABLE 和 INSERT 中加入 password 列。

---

### Task 5: 端到端验证

- [ ] **Step 1: 执行 init-all.sql**

Run: `mysql -u root -p1234 < sql/init-all.sql`
Expected: 所有表创建成功，演示数据插入

- [ ] **Step 2: 启动 shell 后端**

Run: `cd frontend-shell/backend && mvnw spring-boot:run`
Expected: 端口 8085

- [ ] **Step 3: 启动 shell 前端**

Run: `cd frontend-shell/frontend && npm install && npm run dev`
Expected: 端口 5000

- [ ] **Step 4: 测试登录**

```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userId":"2001","password":"123456"}'
```
Expected: `{"success":true,"data":{"token":"...","userName":"杨辉"}}`

- [ ] **Step 5: 浏览器打开 http://localhost:5000**

登录 → 导航页 → 点五张卡片各开一个新标签

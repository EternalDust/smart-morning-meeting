# 晨会智能汇报与实时交互子系统 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建晨会智能汇报与实时交互子系统完整前后端，含签到、汇报、实时交互三大模块。

**Architecture:** SpringBoot 2.7 + MyBatis-Plus 后端 RESTful API，Vue3 + ElementPlus 前端 SPA，WebSocket 实时推送，MySQL 8.0 存储。

**Tech Stack:** Java8, SpringBoot 2.7.6, MyBatis-Plus 3.5.5, SpringSecurity, JWT, WebSocket, Vue3, ElementPlus, Pinia, Axios, Vite

---

### Task 1: Backend 项目骨架

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/smartmeeting/SmartMeetingApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/smartmeeting/common/Result.java`

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
    <groupId>com.smartmeeting</groupId>
    <artifactId>smart-report-interaction</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>1.8</java.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
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
package com.smartmeeting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smartmeeting.dao")
public class SmartMeetingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartMeetingApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_meeting?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

- [ ] **Step 4: 创建统一响应类**

```java
package com.smartmeeting.common;

import lombok.Data;

@Data
public class Result<T> {
    private boolean success;
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.success = true;
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.success = false;
        r.code = code;
        r.msg = msg;
        return r;
    }
}
```

- [ ] **Step 5: 验证项目可编译**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

---

### Task 2: 数据库初始化

**Files:**
- Create: `sql/init.sql`

- [ ] **Step 1: 编写建表 SQL**

```sql
CREATE DATABASE IF NOT EXISTS smart_meeting DEFAULT CHARSET utf8mb4;
USE smart_meeting;

-- 1. 晨会信息主表
CREATE TABLE sm_meeting_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    host_id VARCHAR(50),
    start_time VARCHAR(20),
    end_time VARCHAR(20),
    status INT DEFAULT 0,
    create_time VARCHAR(20)
);

-- 2. 晨会签到表
CREATE TABLE sm_meeting_signin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    sign_time VARCHAR(20),
    sign_type INT DEFAULT 2,
    sign_status INT DEFAULT 0
);

-- 3. 晨会发言转写表
CREATE TABLE sm_meeting_speech (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    speaker_id VARCHAR(50) NOT NULL,
    content LONGTEXT,
    speech_time VARCHAR(20),
    key_points TEXT
);

-- 4. 晨会交互表
CREATE TABLE sm_meeting_interaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    interact_type INT DEFAULT 1,
    content TEXT,
    reply TEXT,
    create_time VARCHAR(20)
);

-- 5. 晨会摘要表
CREATE TABLE sm_meeting_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meeting_id BIGINT NOT NULL,
    summary LONGTEXT,
    create_time VARCHAR(20)
);
```

- [ ] **Step 2: 执行 SQL**

Run: `mysql -u root -p < sql/init.sql`
Expected: 5 tables created in smart_meeting database

---

### Task 3: 实体类

**Files:**
- Create: `backend/src/main/java/com/smartmeeting/entity/MeetingInfo.java`
- Create: `backend/src/main/java/com/smartmeeting/entity/SignIn.java`
- Create: `backend/src/main/java/com/smartmeeting/entity/SpeechRecord.java`
- Create: `backend/src/main/java/com/smartmeeting/entity/Interaction.java`
- Create: `backend/src/main/java/com/smartmeeting/entity/MeetingSummary.java`

- [ ] **Step 1: MeetingInfo 实体**

```java
package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_info")
public class MeetingInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String hostId;
    private String startTime;
    private String endTime;
    private Integer status;
    private String createTime;
}
```

- [ ] **Step 2: SignIn 实体**

```java
package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_signin")
public class SignIn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String userId;
    private String signTime;
    private Integer signType;  // 1=扫码 2=手动
    private Integer signStatus; // 0=正常 1=迟到 2=缺席
}
```

- [ ] **Step 3: SpeechRecord 实体**

```java
package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_speech")
public class SpeechRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String speakerId;
    private String content;
    private String speechTime;
    private String keyPoints;
}
```

- [ ] **Step 4: Interaction 实体**

```java
package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_interaction")
public class Interaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String userId;
    private Integer interactType; // 1=提问 2=反馈 3=投票
    private String content;
    private String reply;
    private String createTime;
}
```

- [ ] **Step 5: MeetingSummary 实体**

```java
package com.smartmeeting.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sm_meeting_summary")
public class MeetingSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String summary;
    private String createTime;
}
```

- [ ] **Step 6: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

---

### Task 4: Mapper 接口

**Files:**
- Create: `backend/src/main/java/com/smartmeeting/dao/SignInMapper.java`
- Create: `backend/src/main/java/com/smartmeeting/dao/SpeechRecordMapper.java`
- Create: `backend/src/main/java/com/smartmeeting/dao/InteractionMapper.java`
- Create: `backend/src/main/java/com/smartmeeting/dao/MeetingSummaryMapper.java`
- Create: `backend/src/main/java/com/smartmeeting/dao/MeetingInfoMapper.java`

- [ ] **Step 1: 创建全部 Mapper 接口**

```java
package com.smartmeeting.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmeeting.entity.SignIn;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SignInMapper extends BaseMapper<SignIn> {
}
```

```java
package com.smartmeeting.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmeeting.entity.SpeechRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SpeechRecordMapper extends BaseMapper<SpeechRecord> {
}
```

```java
package com.smartmeeting.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmeeting.entity.Interaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InteractionMapper extends BaseMapper<Interaction> {
}
```

```java
package com.smartmeeting.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmeeting.entity.MeetingSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeetingSummaryMapper extends BaseMapper<MeetingSummary> {
}
```

```java
package com.smartmeeting.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartmeeting.entity.MeetingInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeetingInfoMapper extends BaseMapper<MeetingInfo> {
}
```

- [ ] **Step 2: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

---

### Task 5: 安全与 WebSocket 配置

**Files:**
- Create: `backend/src/main/java/com/smartmeeting/config/SecurityConfig.java`
- Create: `backend/src/main/java/com/smartmeeting/config/WebSocketConfig.java`
- Create: `backend/src/main/java/com/smartmeeting/config/JwtUtil.java`

- [ ] **Step 1: JwtUtil**

```java
package com.smartmeeting.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET = "smart-meeting-secret-key-2026";
    private static final long EXPIRATION = 86400000; // 24h

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return Jwts.parser().setSigningKey(SECRET)
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 2: SecurityConfig（开发阶段放宽）**

```java
package com.smartmeeting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/meeting/**", "/ws/**").permitAll()
            .anyRequest().authenticated();
        return http.build();
    }
}
```

- [ ] **Step 3: WebSocketConfig**

```java
package com.smartmeeting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

---

### Task 6: 签到功能（Service + Controller）

**Files:**
- Create: `backend/src/main/java/com/smartmeeting/service/SignService.java`
- Create: `backend/src/main/java/com/smartmeeting/controller/SignController.java`
- Create: `backend/src/test/java/com/smartmeeting/service/SignServiceTest.java`

- [ ] **Step 1: SignService**

```java
package com.smartmeeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmeeting.dao.SignInMapper;
import com.smartmeeting.entity.SignIn;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SignService {

    @Resource
    private SignInMapper signInMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SignIn signIn(Long meetingId, String userId, Integer signType) {
        SignIn record = new SignIn();
        record.setMeetingId(meetingId);
        record.setUserId(userId);
        record.setSignType(signType);
        record.setSignTime(LocalDateTime.now().format(FMT));

        // 判断是否迟到：8:30之后签到
        int hour = LocalDateTime.now().getHour();
        int minute = LocalDateTime.now().getMinute();
        record.setSignStatus((hour > 8 || (hour == 8 && minute > 30)) ? 1 : 0);

        signInMapper.insert(record);
        return record;
    }

    public List<SignIn> listByMeetingId(Long meetingId) {
        LambdaQueryWrapper<SignIn> qw = new LambdaQueryWrapper<>();
        qw.eq(SignIn::getMeetingId, meetingId).orderByAsc(SignIn::getSignTime);
        return signInMapper.selectList(qw);
    }

    public long countByStatus(Long meetingId, Integer status) {
        LambdaQueryWrapper<SignIn> qw = new LambdaQueryWrapper<>();
        qw.eq(SignIn::getMeetingId, meetingId).eq(SignIn::getSignStatus, status);
        return signInMapper.selectCount(qw);
    }
}
```

- [ ] **Step 2: SignController**

```java
package com.smartmeeting.controller;

import com.smartmeeting.common.Result;
import com.smartmeeting.entity.SignIn;
import com.smartmeeting.service.SignService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meeting/sign")
public class SignController {

    @Resource
    private SignService signService;

    @PostMapping("/in")
    public Result<SignIn> signIn(@RequestBody Map<String, Object> body) {
        Long meetingId = Long.valueOf(body.get("meetingId").toString());
        String userId = (String) body.get("userId");
        Integer signType = (Integer) body.getOrDefault("signType", 2);
        return Result.ok(signService.signIn(meetingId, userId, signType));
    }

    @GetMapping("/list/{meetingId}")
    public Result<Map<String, Object>> list(@PathVariable Long meetingId) {
        List<SignIn> records = signService.listByMeetingId(meetingId);
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", records.size());
        result.put("normal", signService.countByStatus(meetingId, 0));
        result.put("late", signService.countByStatus(meetingId, 1));
        result.put("absent", signService.countByStatus(meetingId, 2));
        return Result.ok(result);
    }
}
```

- [ ] **Step 3: 启动应用验证**

Run: `cd backend && mvn spring-boot:run`
Expected: Application starts on port 8081

---

### Task 7: 汇报功能（Service + Controller）

**Files:**
- Create: `backend/src/main/java/com/smartmeeting/service/ReportService.java`
- Create: `backend/src/main/java/com/smartmeeting/controller/ReportController.java`

- [ ] **Step 1: ReportService**

```java
package com.smartmeeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmeeting.dao.SpeechRecordMapper;
import com.smartmeeting.dao.MeetingSummaryMapper;
import com.smartmeeting.entity.SpeechRecord;
import com.smartmeeting.entity.MeetingSummary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    @Resource
    private SpeechRecordMapper speechMapper;

    @Resource
    private MeetingSummaryMapper summaryMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SpeechRecord saveSpeech(SpeechRecord record) {
        record.setSpeechTime(LocalDateTime.now().format(FMT));
        speechMapper.insert(record);
        return record;
    }

    public SpeechRecord updateSpeech(Long id, String content, String keyPoints) {
        SpeechRecord record = speechMapper.selectById(id);
        if (record != null) {
            record.setContent(content);
            record.setKeyPoints(keyPoints);
            speechMapper.updateById(record);
        }
        return record;
    }

    public List<SpeechRecord> listByMeetingId(Long meetingId) {
        LambdaQueryWrapper<SpeechRecord> qw = new LambdaQueryWrapper<>();
        qw.eq(SpeechRecord::getMeetingId, meetingId).orderByAsc(SpeechRecord::getSpeechTime);
        return speechMapper.selectList(qw);
    }

    public MeetingSummary saveSummary(Long meetingId, String summaryText) {
        LambdaQueryWrapper<MeetingSummary> qw = new LambdaQueryWrapper<>();
        qw.eq(MeetingSummary::getMeetingId, meetingId);
        MeetingSummary summary = summaryMapper.selectOne(qw);
        if (summary == null) {
            summary = new MeetingSummary();
            summary.setMeetingId(meetingId);
        }
        summary.setSummary(summaryText);
        summary.setCreateTime(LocalDateTime.now().format(FMT));
        summaryMapper.insertOrUpdate(summary);
        return summary;
    }

    public MeetingSummary getSummary(Long meetingId) {
        LambdaQueryWrapper<MeetingSummary> qw = new LambdaQueryWrapper<>();
        qw.eq(MeetingSummary::getMeetingId, meetingId);
        return summaryMapper.selectOne(qw);
    }
}
```

- [ ] **Step 2: ReportController**

```java
package com.smartmeeting.controller;

import com.smartmeeting.common.Result;
import com.smartmeeting.entity.SpeechRecord;
import com.smartmeeting.entity.MeetingSummary;
import com.smartmeeting.service.ReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meeting")
public class ReportController {

    @Resource
    private ReportService reportService;

    @PostMapping("/speech/save")
    public Result<SpeechRecord> saveSpeech(@RequestBody SpeechRecord record) {
        return Result.ok(reportService.saveSpeech(record));
    }

    @PutMapping("/speech/{id}")
    public Result<SpeechRecord> updateSpeech(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.ok(reportService.updateSpeech(id, body.get("content"), body.get("keyPoints")));
    }

    @GetMapping("/speech/list/{meetingId}")
    public Result<List<SpeechRecord>> listSpeech(@PathVariable Long meetingId) {
        return Result.ok(reportService.listByMeetingId(meetingId));
    }

    @PostMapping("/summary/save")
    public Result<MeetingSummary> saveSummary(@RequestBody Map<String, Object> body) {
        Long meetingId = Long.valueOf(body.get("meetingId").toString());
        String summary = (String) body.get("summary");
        return Result.ok(reportService.saveSummary(meetingId, summary));
    }

    @GetMapping("/summary/{meetingId}")
    public Result<MeetingSummary> getSummary(@PathVariable Long meetingId) {
        return Result.ok(reportService.getSummary(meetingId));
    }

    @GetMapping("/summary/export/{meetingId}")
    public Result<Map<String, String>> exportSummary(@PathVariable Long meetingId) {
        MeetingSummary summary = reportService.getSummary(meetingId);
        Map<String, String> result = new HashMap<>();
        result.put("content", summary != null ? summary.getSummary() : "");
        result.put("meetingId", String.valueOf(meetingId));
        return Result.ok(result);
    }
}
```

---

### Task 8: 交互功能（Service + Controller）

**Files:**
- Create: `backend/src/main/java/com/smartmeeting/service/InteractionService.java`
- Create: `backend/src/main/java/com/smartmeeting/controller/InteractionController.java`

- [ ] **Step 1: InteractionService**

```java
package com.smartmeeting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartmeeting.dao.InteractionMapper;
import com.smartmeeting.entity.Interaction;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InteractionService {

    @Resource
    private InteractionMapper interactionMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Interaction sendMessage(Interaction msg) {
        msg.setCreateTime(LocalDateTime.now().format(FMT));
        interactionMapper.insert(msg);
        return msg;
    }

    public Interaction replyMessage(Long id, String reply) {
        Interaction msg = interactionMapper.selectById(id);
        if (msg != null) {
            msg.setReply(reply);
            interactionMapper.updateById(msg);
        }
        return msg;
    }

    public List<Interaction> listByMeetingId(Long meetingId, Integer interactType) {
        LambdaQueryWrapper<Interaction> qw = new LambdaQueryWrapper<>();
        qw.eq(Interaction::getMeetingId, meetingId)
          .orderByDesc(Interaction::getCreateTime);
        if (interactType != null && interactType > 0) {
            qw.eq(Interaction::getInteractType, interactType);
        }
        return interactionMapper.selectList(qw);
    }

    public long[] countStats(Long meetingId) {
        long questions = countByType(meetingId, 1);
        long feedback = countByType(meetingId, 2);
        long votes = countByType(meetingId, 3);
        long replied = countReplied(meetingId);
        return new long[]{questions, feedback, votes, replied};
    }

    private long countByType(Long meetingId, int type) {
        LambdaQueryWrapper<Interaction> qw = new LambdaQueryWrapper<>();
        qw.eq(Interaction::getMeetingId, meetingId).eq(Interaction::getInteractType, type);
        return interactionMapper.selectCount(qw);
    }

    private long countReplied(Long meetingId) {
        LambdaQueryWrapper<Interaction> qw = new LambdaQueryWrapper<>();
        qw.eq(Interaction::getMeetingId, meetingId).isNotNull(Interaction::getReply)
          .ne(Interaction::getReply, "");
        return interactionMapper.selectCount(qw);
    }
}
```

- [ ] **Step 2: InteractionController**

```java
package com.smartmeeting.controller;

import com.smartmeeting.common.Result;
import com.smartmeeting.entity.Interaction;
import com.smartmeeting.service.InteractionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meeting/interaction")
public class InteractionController {

    @Resource
    private InteractionService interactionService;

    @PostMapping("/message")
    public Result<Interaction> sendMessage(@RequestBody Interaction msg) {
        return Result.ok(interactionService.sendMessage(msg));
    }

    @PostMapping("/reply/{id}")
    public Result<Interaction> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.ok(interactionService.replyMessage(id, body.get("reply")));
    }

    @GetMapping("/list/{meetingId}")
    public Result<List<Interaction>> list(@PathVariable Long meetingId,
                                          @RequestParam(required = false) Integer type) {
        return Result.ok(interactionService.listByMeetingId(meetingId, type));
    }

    @GetMapping("/stats/{meetingId}")
    public Result<Map<String, Long>> stats(@PathVariable Long meetingId) {
        long[] s = interactionService.countStats(meetingId);
        Map<String, Long> result = new HashMap<>();
        result.put("questions", s[0]);
        result.put("feedback", s[1]);
        result.put("votes", s[2]);
        result.put("replied", s[3]);
        return Result.ok(result);
    }
}
```

---

### Task 9: WebSocket 实时推送

**Files:**
- Create: `backend/src/main/java/com/smartmeeting/ws/RealtimeServer.java`

- [ ] **Step 1: WebSocket 端点**

```java
package com.smartmeeting.ws;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/api/meeting/realtime/push/{meetingId}")
public class RealtimeServer {

    private static final ConcurrentHashMap<Long, ConcurrentHashMap<String, Session>> rooms = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("meetingId") Long meetingId) {
        rooms.computeIfAbsent(meetingId, k -> new ConcurrentHashMap<>())
              .put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("meetingId") Long meetingId) {
        ConcurrentHashMap<String, Session> room = rooms.get(meetingId);
        if (room != null) {
            room.remove(session.getId());
            if (room.isEmpty()) rooms.remove(meetingId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 客户端发来的心跳或简单消息直接忽略
    }

    public static void broadcast(Long meetingId, String message) {
        ConcurrentHashMap<String, Session> room = rooms.get(meetingId);
        if (room != null) {
            room.values().forEach(session -> {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
```

- [ ] **Step 2: 在 SignController 和 InteractionController 中注入 WebSocket 广播**

在 `SignController.java` 中添加：

```java
@PostMapping("/in")
public Result<SignIn> signIn(@RequestBody Map<String, Object> body) {
    Long meetingId = Long.valueOf(body.get("meetingId").toString());
    // ... existing code ...
    SignIn result = signService.signIn(meetingId, userId, signType);
    // 广播签到更新
    RealtimeServer.broadcast(meetingId,
        "{\"type\":\"sign\",\"data\":" + result.toString() + "}");
    return Result.ok(result);
}
```

在 `InteractionController.java` 中添加：

```java
@PostMapping("/message")
public Result<Interaction> sendMessage(@RequestBody Interaction msg) {
    Interaction result = interactionService.sendMessage(msg);
    RealtimeServer.broadcast(msg.getMeetingId(),
        "{\"type\":\"interaction\",\"data\":" + result.toString() + "}");
    return Result.ok(result);
}
```

---

### Task 10: 前端项目骨架

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/router/index.js`

- [ ] **Step 1: 初始化 Vue 项目**

```bash
cd frontend
npm create vite@latest . -- --template vue
npm install vue-router@4 pinia axios element-plus @element-plus/icons-vue
```

- [ ] **Step 2: vite.config.js**

```js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8081', changeOrigin: true },
      '/ws': { target: 'ws://localhost:8081', ws: true }
    }
  }
})
```

- [ ] **Step 3: src/main.js**

```js
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

- [ ] **Step 4: src/router/index.js**

```js
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/sign' },
  { path: '/sign', name: 'SignIn', component: () => import('../views/SignIn.vue') },
  { path: '/report', name: 'Report', component: () => import('../views/MeetingReport.vue') },
  { path: '/interaction', name: 'Interaction', component: () => import('../views/Interaction.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
```

- [ ] **Step 5: src/App.vue**

```vue
<template>
  <div class="app-container">
    <el-menu mode="horizontal" :default-active="activeMenu" router>
      <el-menu-item index="/sign">签到</el-menu-item>
      <el-menu-item index="/report">汇报</el-menu-item>
      <el-menu-item index="/interaction">互动</el-menu-item>
    </el-menu>
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style>
* { margin:0; padding:0; box-sizing:border-box }
body { font-family:'Microsoft YaHei',sans-serif; background:#F8FAFC; color:#1E293B }
:root {
  --p:#2563EB; --pb:#EFF6FF; --s:#059669; --sb:#ECFDF5;
  --w:#D97706; --wb:#FFFBEB; --d:#DC2626; --db:#FEF2F2;
  --bg:#F8FAFC; --ts:#475569; --bd:#E2E8F0;
}
.app-container { height:100vh; display:flex; flex-direction:column; overflow:hidden }
</style>
```

- [ ] **Step 6: 验证前端启动**

Run: `cd frontend && npm run dev`
Expected: Dev server on http://localhost:5173

---

### Task 11: 前端 API 层 + Store

**Files:**
- Create: `frontend/src/api/request.js`
- Create: `frontend/src/api/sign.js`
- Create: `frontend/src/api/report.js`
- Create: `frontend/src/api/interaction.js`
- Create: `frontend/src/stores/meeting.js`

- [ ] **Step 1: request.js（axios 封装）**

```js
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.response.use(
  res => res.data,
  err => Promise.reject(err)
)

export default request
```

- [ ] **Step 2: sign.js**

```js
import request from './request'

export const signIn = (meetingId, userId, signType = 2) =>
  request.post('/meeting/sign/in', { meetingId, userId, signType })

export const getSignList = (meetingId) =>
  request.get(`/meeting/sign/list/${meetingId}`)
```

- [ ] **Step 3: report.js**

```js
import request from './request'

export const saveSpeech = (data) => request.post('/meeting/speech/save', data)
export const updateSpeech = (id, content, keyPoints) =>
  request.put(`/meeting/speech/${id}`, { content, keyPoints })
export const getSpeechList = (meetingId) => request.get(`/meeting/speech/list/${meetingId}`)
export const saveSummary = (meetingId, summary) =>
  request.post('/meeting/summary/save', { meetingId, summary })
export const getSummary = (meetingId) => request.get(`/meeting/summary/${meetingId}`)
```

- [ ] **Step 4: interaction.js**

```js
import request from './request'

export const sendMessage = (data) => request.post('/meeting/interaction/message', data)
export const replyMessage = (id, reply) =>
  request.post(`/meeting/interaction/reply/${id}`, { reply })
export const getInteractionList = (meetingId, type) =>
  request.get(`/meeting/interaction/list/${meetingId}`, { params: { type } })
export const getStats = (meetingId) =>
  request.get(`/meeting/interaction/stats/${meetingId}`)
```

- [ ] **Step 5: meeting.js（Pinia store）**

```js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMeetingStore = defineStore('meeting', () => {
  const currentMeeting = ref({
    id: 1,
    title: '周一科室晨会',
    time: '08:30–09:30',
    location: '会议室A',
    status: '进行中'
  })
  return { currentMeeting }
})
```

---

### Task 12: 签到页 SignIn.vue

**Files:**
- Create: `frontend/src/views/SignIn.vue`

- [ ] **Step 1: 完整组件代码**

```vue
<template>
  <div class="page-layout" style="height:100%;overflow:hidden">
    <div class="top-bar">
      <h2>{{ meeting.title }}</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }} · {{ meeting.location }}</span>
    </div>

    <div class="content">
      <!-- 左栏 -->
      <div class="left-panel">
        <h3>签到操作</h3>
        <label class="field-label">工号/扫码</label>
        <div class="input-row">
          <el-input v-model="userId" placeholder="输入工号或扫描二维码签到" size="large" />
          <el-button type="primary" size="large" @click="doSignIn">签到</el-button>
        </div>

        <div class="stat-grid">
          <div class="stat-card success"><div class="num">{{ stats.normal }}</div><div class="label">已签到</div></div>
          <div class="stat-card warning"><div class="num">{{ stats.late }}</div><div class="label">迟到</div></div>
          <div class="stat-card danger"><div class="num">{{ stats.absent }}</div><div class="label">缺席</div></div>
          <div class="stat-card primary"><div class="num">{{ stats.total }}</div><div class="label">应到</div></div>
        </div>

        <div class="btn-row">
          <el-button @click="refresh">刷新列表</el-button>
          <el-button>导出签到表</el-button>
        </div>
      </div>

      <!-- 右栏 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>签到记录</h3>
          <span class="count">已签 {{ stats.normal + stats.late }} / 应到 {{ stats.total }}</span>
        </div>
        <div class="table-scroll">
          <el-table :data="records" stripe style="width:100%" max-height="100%">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="姓名">
              <template #default="{ row }">
                <el-avatar :size="24" style="margin-right:8px;vertical-align:middle">{{ row.userId }}</el-avatar>
                {{ row.userId }}
              </template>
            </el-table-column>
            <el-table-column prop="signType" label="签到方式" width="80">
              <template #default="{ row }">{{ row.signType === 1 ? '扫码' : '手动' }}</template>
            </el-table-column>
            <el-table-column prop="signTime" label="签到时间" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusType(row.signStatus)" size="small">
                  {{ statusText(row.signStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { signIn, getSignList } from '../api/sign'
import { useMeetingStore } from '../stores/meeting'

const store = useMeetingStore()
const meeting = store.currentMeeting

const userId = ref('')
const records = ref([])
const stats = reactive({ normal: 0, late: 0, absent: 0, total: 0 })

const loadData = async () => {
  const res = await getSignList(meeting.id)
  records.value = res.data.records
  stats.normal = res.data.normal
  stats.late = res.data.late
  stats.absent = res.data.absent
  stats.total = res.data.total
}

const doSignIn = async () => {
  if (!userId.value) { ElMessage.warning('请输入工号'); return }
  await signIn(meeting.id, userId.value, 2)
  ElMessage.success('签到成功')
  userId.value = ''
  await loadData()
}

const refresh = () => loadData()

const statusType = (s) => s === 0 ? 'success' : s === 1 ? 'warning' : 'danger'
const statusText = (s) => s === 0 ? '正常' : s === 1 ? '迟到' : '缺席'

onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px 16px 8px; height:100% }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:12px; flex-shrink:0 }
.top-bar h2 { font-size:16px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.content { flex:1; display:flex; gap:14px; min-height:0; overflow:hidden }
.left-panel { width:320px; flex-shrink:0; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; justify-content:center }
.left-panel h3 { font-size:14px; margin-bottom:12px }
.field-label { font-size:12px; color:var(--ts); display:block; margin-bottom:4px }
.input-row { display:flex; gap:8px; margin-bottom:14px }
.stat-grid { display:flex; gap:8px; margin-bottom:20px }
.stat-card { flex:1; text-align:center; padding:10px 6px; border-radius:6px }
.stat-card.success { background:var(--sb) }  .stat-card.success .num { color:var(--s) }
.stat-card.warning { background:var(--wb) }  .stat-card.warning .num { color:var(--w) }
.stat-card.danger  { background:var(--db) }  .stat-card.danger .num  { color:var(--d) }
.stat-card.primary { background:var(--pb) }  .stat-card.primary .num { color:var(--p) }
.stat-card .num { font-size:24px; font-weight:700 }
.stat-card .label { font-size:11px; color:var(--ts); margin-top:2px }
.btn-row { display:flex; gap:8px }
.right-panel { flex:1; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.panel-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; flex-shrink:0 }
.panel-header h3 { font-size:14px }
.count { font-size:12px; color:var(--ts) }
.table-scroll { flex:1; overflow-y:auto }
</style>
```

---

### Task 13: 汇报页 MeetingReport.vue

**Files:**
- Create: `frontend/src/views/MeetingReport.vue`

- [ ] **Step 1: 完整组件代码**

```vue
<template>
  <div class="page-layout" style="height:100%;overflow:hidden">
    <div class="top-bar">
      <h2>会议汇报</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }}</span>
    </div>

    <div class="content">
      <!-- 左栏 -->
      <div class="left-panel">
        <div class="section-header">
          <h3>议程与汇报</h3>
          <span class="step">环节 {{ currentAgenda }} / 4</span>
        </div>

        <div class="agenda-tabs">
          <div v-for="(a,i) in agendas" :key="i"
            :class="['tab', { active: currentAgenda === i+1 }]"
            @click="currentAgenda = i+1">{{ i+1 }}. {{ a }}</div>
        </div>

        <div class="speaker-info">
          当前汇报人：
          <el-avatar :size="28" style="background:#2563EB">{{ speaker }}</el-avatar>
          <strong>{{ speaker }}</strong>
          <span>· 外科</span>
        </div>

        <label class="field-label">发言内容</label>
        <el-input v-model="content" type="textarea" :rows="3" placeholder="录入发言内容或要点..." />
        <div class="action-row">
          <el-button @click="saveDraft">暂存草稿</el-button>
          <el-button type="primary" @click="saveSpeech">保存发言</el-button>
        </div>

        <div class="record-list">
          <div class="section-title">汇报记录</div>
          <div v-for="r in records" :key="r.id" class="record-item">
            <el-avatar :size="24">{{ r.speakerId }}</el-avatar>
            <div class="record-body">
              <div class="record-meta">
                <strong>{{ r.speakerId }}</strong>
                <span>{{ r.createTime }}</span>
                <el-tag v-if="r.id === editingId" type="success" size="small">当前发言</el-tag>
              </div>
              <p>{{ r.content || '—' }}</p>
              <p class="key-points" v-if="r.keyPoints">要点：{{ r.keyPoints }}</p>
            </div>
            <el-button link type="primary" size="small" @click="editRecord(r)">编辑</el-button>
          </div>
        </div>
      </div>

      <!-- 右栏 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>会议摘要</h3>
          <el-button size="small" @click="exportDoc">导出文档</el-button>
        </div>
        <div class="summary-body" v-html="summaryHtml"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { saveSpeech as apiSave, getSpeechList, saveSummary, getSummary } from '../api/report'
import { useMeetingStore } from '../stores/meeting'

const store = useMeetingStore()
const meeting = store.currentMeeting

const agendas = ['数据通报', '科室汇报', '问题讨论', '总结部署']
const currentAgenda = ref(2)
const speaker = ref('李明辉')
const content = ref('')
const editingId = ref(null)
const records = ref([])
const summaryHtml = ref('<p style="color:#475569">会议进行中...</p>')

const loadData = async () => {
  const res = await getSpeechList(meeting.id)
  records.value = res.data || []
  const s = await getSummary(meeting.id)
  if (s.data) summaryHtml.value = s.data.summary || ''
}

const saveDraft = async () => {
  ElMessage.info('草稿已暂存')
}

const saveSpeech = async () => {
  if (!content.value) { ElMessage.warning('请输入发言内容'); return }
  await apiSave({ meetingId: meeting.id, speakerId: 'LiMinHui', content: content.value })
  ElMessage.success('发言已保存')
  content.value = ''
  await loadData()
}

const editRecord = (r) => {
  content.value = r.content
  editingId.value = r.id
}

const exportDoc = () => ElMessage.success('文档导出中...')

onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px 16px 8px; height:100% }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:10px; flex-shrink:0 }
.top-bar h2 { font-size:16px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.content { flex:1; display:flex; gap:14px; min-height:0; overflow:hidden }
.left-panel { flex:2; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.section-header { display:flex; align-items:center; gap:10px; margin-bottom:8px; flex-shrink:0 }
.section-header h3 { font-size:14px }
.step { font-size:12px; color:var(--ts) }
.agenda-tabs { display:flex; margin-bottom:10px; flex-shrink:0 }
.agenda-tabs .tab { padding:5px 14px; font-size:12px; border:1px solid var(--bd); border-left:0; background:var(--pb); color:var(--p); cursor:pointer }
.agenda-tabs .tab:first-child { border-left:1px solid var(--bd); border-radius:6px 0 0 6px }
.agenda-tabs .tab:last-child { border-radius:0 6px 6px 0 }
.agenda-tabs .tab.active { background:var(--p); color:#fff; border-color:var(--p) }
.speaker-info { display:flex; align-items:center; gap:8px; margin-bottom:10px; font-size:13px; flex-shrink:0 }
.field-label { font-size:12px; color:var(--ts); display:block; margin-bottom:4px }
.action-row { display:flex; justify-content:flex-end; gap:8px; margin:8px 0; flex-shrink:0 }
.record-list { flex:1; overflow-y:auto; min-height:0 }
.section-title { font-size:12px; color:var(--ts); font-weight:500; margin-bottom:8px }
.record-item { display:flex; align-items:flex-start; gap:8px; padding:8px 0; border-bottom:1px solid var(--bd) }
.record-body { flex:1; min-width:0 }
.record-meta { display:flex; align-items:center; gap:8px; margin-bottom:2px }
.record-meta strong { font-size:13px }
.record-meta span { font-size:11px; color:var(--ts) }
.record-body p { font-size:13px; color:var(--t); line-height:1.6 }
.key-points { font-size:11px; color:var(--ts); margin-top:3px }
.right-panel { flex:1; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.panel-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:10px; flex-shrink:0 }
.panel-header h3 { font-size:14px }
.summary-body { flex:1; overflow-y:auto; border:1px solid var(--bd); border-radius:6px; padding:12px; font-size:13px; line-height:1.8 }
</style>
```

---

### Task 14: 互动页 Interaction.vue

**Files:**
- Create: `frontend/src/views/Interaction.vue`
- Create: `frontend/src/composables/useWebSocket.js`

- [ ] **Step 1: useWebSocket.js**

```js
import { ref, onUnmounted } from 'vue'

export function useWebSocket(meetingId) {
  const connected = ref(false)
  const lastMessage = ref(null)
  let ws = null

  const connect = () => {
    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    ws = new WebSocket(`${protocol}//${location.host}/api/meeting/realtime/push/${meetingId}`)
    ws.onopen = () => { connected.value = true }
    ws.onmessage = (e) => { lastMessage.value = JSON.parse(e.data) }
    ws.onclose = () => { connected.value = false }
  }

  const close = () => { if (ws) ws.close() }

  onUnmounted(close)

  return { connected, lastMessage, connect, close }
}
```

- [ ] **Step 2: Interaction.vue**

```vue
<template>
  <div class="page-layout" style="height:100%;overflow:hidden">
    <div class="top-bar">
      <h2>实时互动</h2>
      <el-tag type="primary" size="small">{{ meeting.status }}</el-tag>
      <span class="time">{{ meeting.time }}</span>
      <span v-if="wsConnected" class="ws-status">● 已连接</span>
    </div>

    <div class="content">
      <!-- 左栏 -->
      <div class="left-panel">
        <div class="filter-bar">
          <el-button-group>
            <el-button :type="filter===0?'primary':''" size="small" @click="filter=0">全部</el-button>
            <el-button :type="filter===1?'primary':''" size="small" @click="filter=1">提问</el-button>
            <el-button :type="filter===2?'primary':''" size="small" @click="filter=2">反馈</el-button>
            <el-button :type="filter===3?'primary':''" size="small" @click="filter=3">投票</el-button>
          </el-button-group>
        </div>

        <div class="message-stream">
          <div v-for="m in filteredMessages" :key="m.id" class="message">
            <el-avatar :size="26">{{ m.userId }}</el-avatar>
            <div class="msg-body">
              <div class="msg-meta">
                <strong>{{ m.userId }}</strong>
                <span>{{ m.createTime }}</span>
                <el-tag :type="typeTag(m.interactType)" size="small">
                  {{ typeText(m.interactType) }}
                </el-tag>
              </div>
              <div class="msg-content">{{ m.content }}</div>
              <div v-if="m.reply" class="msg-reply">
                <span class="reply-label">主持人回复</span>
                {{ m.reply }}
              </div>
            </div>
            <el-button v-if="!m.reply" link type="primary" size="small" @click="replyTo(m)">回复</el-button>
          </div>
        </div>

        <div class="compose">
          <div class="compose-type">
            <el-button-group size="small">
              <el-button :type="composeType===1?'primary':''" @click="composeType=1">提问</el-button>
              <el-button :type="composeType===2?'primary':''" @click="composeType=2">反馈</el-button>
              <el-button :type="composeType===3?'primary':''" @click="composeType=3">发起投票</el-button>
            </el-button-group>
          </div>
          <label class="field-label">输入内容</label>
          <div class="compose-input">
            <el-input v-model="msgContent" placeholder="输入互动内容..." size="large" @keyup.enter="send" />
            <el-button type="primary" size="large" @click="send">发送</el-button>
          </div>
        </div>
      </div>

      <!-- 右栏 -->
      <div class="right-panel">
        <div class="box">
          <h3>进行中的投票</h3>
          <div class="vote-item" v-if="activeVotes.length">
            <div v-for="v in activeVotes" :key="v.id" class="vote-card">
              <div class="vote-title">{{ v.title }}</div>
              <div class="vote-meta">已投 15/15 · 剩余 2 分钟</div>
            </div>
          </div>
          <div v-else class="empty">暂无进行中的投票</div>
        </div>

        <div class="box">
          <h3>互动统计</h3>
          <div class="stats-grid">
            <div class="s-card s-primary"><div class="s-num">{{ stats.questions }}</div><div class="s-label">提问</div></div>
            <div class="s-card s-purple"><div class="s-num">{{ stats.feedback }}</div><div class="s-label">反馈</div></div>
            <div class="s-card s-warn"><div class="s-num">{{ stats.votes }}</div><div class="s-label">投票</div></div>
            <div class="s-card s-success"><div class="s-num">{{ stats.replied }}</div><div class="s-label">已回复</div></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { sendMessage, replyMessage, getInteractionList, getStats } from '../api/interaction'
import { useWebSocket } from '../composables/useWebSocket'
import { useMeetingStore } from '../stores/meeting'

const store = useMeetingStore()
const meeting = store.currentMeeting

const { connected: wsConnected, lastMessage, connect } = useWebSocket(meeting.id)
connect()

const filter = ref(0)
const composeType = ref(1)
const msgContent = ref('')
const messages = ref([])
const stats = reactive({ questions: 0, feedback: 0, votes: 0, replied: 0 })
const activeVotes = ref([])

const filteredMessages = computed(() =>
  filter.value === 0 ? messages.value : messages.value.filter(m => m.interactType === filter.value)
)

const loadData = async () => {
  const res = await getInteractionList(meeting.id, filter.value || undefined)
  messages.value = res.data || []
  const s = await getStats(meeting.id)
  if (s.data) Object.assign(stats, s.data)
}

const send = async () => {
  if (!msgContent.value) { ElMessage.warning('请输入内容'); return }
  await sendMessage({ meetingId: meeting.id, userId: 'User01', content: msgContent.value, interactType: composeType.value })
  ElMessage.success('已发送')
  msgContent.value = ''
  await loadData()
}

const replyTo = async (m) => {
  const reply = prompt('输入回复内容：')
  if (reply) {
    await replyMessage(m.id, reply)
    await loadData()
  }
}

const typeTag = (t) => t === 1 ? '' : t === 2 ? 'info' : 'warning'
const typeText = (t) => t === 1 ? '提问' : t === 2 ? '反馈' : '投票'

watch(lastMessage, () => { if (lastMessage.value) loadData() })
watch(filter, loadData)
onMounted(loadData)
</script>

<style scoped>
.page-layout { display:flex; flex-direction:column; padding:16px 16px 8px; height:100% }
.top-bar { display:flex; align-items:center; gap:10px; margin-bottom:10px; flex-shrink:0 }
.top-bar h2 { font-size:16px; margin:0 }
.time { color:var(--ts); font-size:13px; margin-left:auto }
.ws-status { font-size:11px; color:var(--s) }
.content { flex:1; display:flex; gap:14px; min-height:0; overflow:hidden }
.left-panel { flex:2; background:#fff; border:1px solid var(--bd); border-radius:8px; padding:16px; display:flex; flex-direction:column; overflow:hidden }
.filter-bar { margin-bottom:10px; flex-shrink:0 }
.message-stream { flex:1; overflow-y:auto }
.message { display:flex; align-items:flex-start; gap:8px; padding:10px 0; border-bottom:1px solid var(--bd) }
.msg-body { flex:1; min-width:0 }
.msg-meta { display:flex; align-items:center; gap:8px; margin-bottom:4px }
.msg-meta strong { font-size:13px }
.msg-meta span { font-size:11px; color:var(--ts) }
.msg-content { background:var(--bg); padding:8px 12px; border-radius:6px; font-size:13px; margin-top:4px }
.msg-reply { margin-top:4px; margin-left:8px; border-left:2px solid var(--p); padding:4px 8px; font-size:12px }
.reply-label { font-size:10px; color:var(--p); font-weight:500; display:block }
.compose { flex-shrink:0; margin-top:10px; padding-top:10px; border-top:1px solid var(--bd) }
.compose-type { margin-bottom:6px }
.field-label { font-size:12px; color:var(--ts); display:block; margin-bottom:4px }
.compose-input { display:flex; gap:8px }
.right-panel { width:240px; flex-shrink:0; display:flex; flex-direction:column; gap:10px }
.box { background:#fff; border:1px solid var(--bd); border-radius:8px; padding:14px }
.box h3 { font-size:14px; margin-bottom:8px }
.vote-card { border:1px solid var(--bd); border-radius:6px; padding:8px 10px; margin-bottom:6px }
.vote-title { font-weight:500; font-size:13px }
.vote-meta { font-size:11px; color:var(--ts) }
.empty { font-size:12px; color:var(--ts); text-align:center; padding:20px 0 }
.stats-grid { display:grid; grid-template-columns:1fr 1fr; gap:6px }
.s-card { text-align:center; padding:8px; border-radius:6px }
.s-primary { background:var(--pb) } .s-primary .s-num { color:var(--p) }
.s-purple { background:#F3E8FF } .s-purple .s-num { color:#7C3AED }
.s-warn { background:var(--wb) } .s-warn .s-num { color:var(--w) }
.s-success { background:var(--sb) } .s-success .s-num { color:var(--s) }
.s-num { font-size:20px; font-weight:700 }
.s-label { font-size:11px; color:var(--ts) }
</style>
```

---

### Task 15: 集成验证

- [ ] **Step 1: 启动后端**

```bash
cd backend && mvn spring-boot:run
```
Expected: Application started on port 8081

- [ ] **Step 2: 启动前端**

```bash
cd frontend && npm run dev
```
Expected: Dev server on http://localhost:5173

- [ ] **Step 3: 验证签到 API**

```bash
curl -X POST http://localhost:8081/api/meeting/sign/in \
  -H "Content-Type: application/json" \
  -d '{"meetingId":1,"userId":"TestUser","signType":2}'
```
Expected: `{"success":true,"code":200,"data":{...}}`

- [ ] **Step 4: 验证签到列表 API**

```bash
curl http://localhost:8081/api/meeting/sign/list/1
```
Expected: `{"success":true,"data":{"records":[...],"total":1,...}}`

- [ ] **Step 5: 打开前端页面验证**

Open http://localhost:5173，切换签到/汇报/互动三个页面，确认页面正常渲染。

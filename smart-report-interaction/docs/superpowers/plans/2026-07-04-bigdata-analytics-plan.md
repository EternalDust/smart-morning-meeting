# 大数据分析模块实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 smart-report-interaction 子系统内新增大数据分析层，Spark 批处理 + REST API + Vue 前端。

**Architecture:** Spark 独立 JAR 通过 JDBC 读 MySQL 现有业务表，聚合计算后写回三张新分析表；Spring Boot 查询 API 直接读分析表；Vue 前端用 ECharts 展示。

**Tech Stack:** Spring Boot 2.7, MyBatis-Plus 3.5.x, Spark 2.4.7 (Scala 2.11), Vue 3 + Element Plus + ECharts

---

### Task 1: 创建分析表 DDL

**Files:**
- Create: `smart-report-interaction/sql/analytics_init.sql`

- [ ] **Step 1: 创建 SQL 文件**

```sql
-- 大数据分析结果表
-- 先执行建表，再跑 Spark 作业灌数据，后端 API 直接查询

CREATE TABLE IF NOT EXISTS sm_meeting_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    meeting_title VARCHAR(200),
    meeting_date VARCHAR(20),
    should_attend INT DEFAULT 0,
    actual_attend INT DEFAULT 0,
    attend_rate DECIMAL(5,2) DEFAULT 0,
    normal_count INT DEFAULT 0,
    late_count INT DEFAULT 0,
    speech_count INT DEFAULT 0,
    interaction_count INT DEFAULT 0,
    quality_score DECIMAL(5,2) DEFAULT 0,
    created_at VARCHAR(20),
    updated_at VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sm_department_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department VARCHAR(100),
    meeting_count INT DEFAULT 0,
    avg_attend_rate DECIMAL(5,2) DEFAULT 0,
    total_speech_count INT DEFAULT 0,
    total_interaction_count INT DEFAULT 0,
    period_start VARCHAR(20),
    period_end VARCHAR(20),
    updated_at VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sm_member_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL,
    user_name VARCHAR(50),
    department VARCHAR(100),
    total_meetings INT DEFAULT 0,
    attended_count INT DEFAULT 0,
    attend_rate DECIMAL(5,2) DEFAULT 0,
    normal_count INT DEFAULT 0,
    late_count INT DEFAULT 0,
    speech_count INT DEFAULT 0,
    interaction_count INT DEFAULT 0,
    last_updated VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 执行建表**

```bash
"C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe" -u root -p1234 smart_meeting --default-character-set=utf8mb4 < smart-report-interaction/sql/analytics_init.sql
```

- [ ] **Step 3: 验证表已创建**

```bash
"C:/Program Files/MySQL/MySQL Server 8.0/bin/mysql.exe" -u root -p1234 smart_meeting -e "SHOW TABLES LIKE 'sm_%analytics%'"
```

预期输出：三张表。

- [ ] **Step 4: 提交**

```bash
git add smart-report-interaction/sql/analytics_init.sql
git commit -m "feat: add analytics result tables DDL"
```

---

### Task 2: 创建 Entity 和 Mapper

**Files:**
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/entity/MeetingAnalytics.java`
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/entity/DepartmentAnalytics.java`
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/entity/MemberAnalytics.java`
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/dao/MeetingAnalyticsMapper.java`
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/dao/DepartmentAnalyticsMapper.java`
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/dao/MemberAnalyticsMapper.java`

- [ ] **Step 1: 创建 MeetingAnalytics.java**

```java
package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_meeting_analytics")
public class MeetingAnalytics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long meetingId;
    private String meetingTitle;
    private String meetingDate;
    private Integer shouldAttend;
    private Integer actualAttend;
    private BigDecimal attendRate;
    private Integer normalCount;
    private Integer lateCount;
    private Integer speechCount;
    private Integer interactionCount;
    private BigDecimal qualityScore;
    private String createdAt;
    private String updatedAt;
}
```

- [ ] **Step 2: 创建 DepartmentAnalytics.java**

```java
package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_department_analytics")
public class DepartmentAnalytics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String department;
    private Integer meetingCount;
    private BigDecimal avgAttendRate;
    private Integer totalSpeechCount;
    private Integer totalInteractionCount;
    private String periodStart;
    private String periodEnd;
    private String updatedAt;
}
```

- [ ] **Step 3: 创建 MemberAnalytics.java**

```java
package com.huadi.smm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("sm_member_analytics")
public class MemberAnalytics {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String userName;
    private String department;
    private Integer totalMeetings;
    private Integer attendedCount;
    private BigDecimal attendRate;
    private Integer normalCount;
    private Integer lateCount;
    private Integer speechCount;
    private Integer interactionCount;
    private String lastUpdated;
}
```

- [ ] **Step 4: 创建 Mapper 接口**

```java
package com.huadi.smm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.entity.MeetingAnalytics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MeetingAnalyticsMapper extends BaseMapper<MeetingAnalytics> {
}
```

```java
package com.huadi.smm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.entity.DepartmentAnalytics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentAnalyticsMapper extends BaseMapper<DepartmentAnalytics> {
}
```

```java
package com.huadi.smm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadi.smm.entity.MemberAnalytics;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberAnalyticsMapper extends BaseMapper<MemberAnalytics> {
}
```

- [ ] **Step 5: 编译验证**

```bash
cd smart-report-interaction/backend && ./mvnw compile -q
```

- [ ] **Step 6: 提交**

```bash
git add smart-report-interaction/backend/src/main/java/com/huadi/smm/entity/MeetingAnalytics.java smart-report-interaction/backend/src/main/java/com/huadi/smm/entity/DepartmentAnalytics.java smart-report-interaction/backend/src/main/java/com/huadi/smm/entity/MemberAnalytics.java smart-report-interaction/backend/src/main/java/com/huadi/smm/dao/
git commit -m "feat: add analytics entity and mapper classes"
```

---

### Task 3: 创建 AnalyticsService

**Files:**
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/service/AnalyticsService.java`

- [ ] **Step 1: 创建 AnalyticsService.java**

```java
package com.huadi.smm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huadi.smm.dao.DepartmentAnalyticsMapper;
import com.huadi.smm.dao.MeetingAnalyticsMapper;
import com.huadi.smm.dao.MemberAnalyticsMapper;
import com.huadi.smm.entity.DepartmentAnalytics;
import com.huadi.smm.entity.MeetingAnalytics;
import com.huadi.smm.entity.MemberAnalytics;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AnalyticsService {

    @Resource
    private MeetingAnalyticsMapper meetingAnalyticsMapper;

    @Resource
    private DepartmentAnalyticsMapper departmentAnalyticsMapper;

    @Resource
    private MemberAnalyticsMapper memberAnalyticsMapper;

    public MeetingAnalytics getByMeetingId(Long meetingId) {
        LambdaQueryWrapper<MeetingAnalytics> qw = new LambdaQueryWrapper<>();
        qw.eq(MeetingAnalytics::getMeetingId, meetingId);
        return meetingAnalyticsMapper.selectOne(qw);
    }

    public List<MeetingAnalytics> getMeetingTrend() {
        LambdaQueryWrapper<MeetingAnalytics> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(MeetingAnalytics::getMeetingDate);
        return meetingAnalyticsMapper.selectList(qw);
    }

    public List<DepartmentAnalytics> getDepartmentRanking() {
        LambdaQueryWrapper<DepartmentAnalytics> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(DepartmentAnalytics::getAvgAttendRate);
        return departmentAnalyticsMapper.selectList(qw);
    }

    public MemberAnalytics getByUserId(String userId) {
        LambdaQueryWrapper<MemberAnalytics> qw = new LambdaQueryWrapper<>();
        qw.eq(MemberAnalytics::getUserId, userId);
        return memberAnalyticsMapper.selectOne(qw);
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd smart-report-interaction/backend && ./mvnw compile -q
```

- [ ] **Step 3: 提交**

```bash
git add smart-report-interaction/backend/src/main/java/com/huadi/smm/service/AnalyticsService.java
git commit -m "feat: add AnalyticsService"
```

---

### Task 4: 创建 AnalyticsController

**Files:**
- Create: `smart-report-interaction/backend/src/main/java/com/huadi/smm/controller/AnalyticsController.java`

- [ ] **Step 1: 创建 AnalyticsController.java**

```java
package com.huadi.smm.controller;

import com.huadi.smm.common.Result;
import com.huadi.smm.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Resource
    private AnalyticsService analyticsService;

    @GetMapping("/meeting/{meetingId}")
    public Result<?> meetingDetail(@PathVariable Long meetingId) {
        return Result.ok(analyticsService.getByMeetingId(meetingId));
    }

    @GetMapping("/meetings/trend")
    public Result<?> meetingTrend() {
        return Result.ok(analyticsService.getMeetingTrend());
    }

    @GetMapping("/departments")
    public Result<?> departments() {
        return Result.ok(analyticsService.getDepartmentRanking());
    }

    @GetMapping("/member/{userId}")
    public Result<?> memberProfile(@PathVariable String userId) {
        return Result.ok(analyticsService.getByUserId(userId));
    }
}
```

- [ ] **Step 2: 编译验证**

```bash
cd smart-report-interaction/backend && ./mvnw compile -q
```

- [ ] **Step 3: 提交**

```bash
git add smart-report-interaction/backend/src/main/java/com/huadi/smm/controller/AnalyticsController.java
git commit -m "feat: add AnalyticsController"
```

---

### Task 5: 创建 Spark 分析作业

**Files:**
- Create: `smart-report-interaction/spark/pom.xml`
- Create: `smart-report-interaction/spark/src/main/scala/com/huadi/smm/spark/MeetingAnalyticsJob.scala`
- Create: `smart-report-interaction/spark/submit.sh`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.huadi.smm</groupId>
    <artifactId>spark-analytics</artifactId>
    <version>1.0</version>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <scala.version>2.11.12</scala.version>
        <spark.version>2.4.7</spark.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
            <version>${scala.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.11</artifactId>
            <version>${spark.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-sql_2.11</artifactId>
            <version>${spark.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>net.alchim31.maven</groupId>
                <artifactId>scala-maven-plugin</artifactId>
                <version>4.3.0</version>
                <executions>
                    <execution>
                        <goals><goal>compile</goal></goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals><goal>shade</goal></goals>
                        <configuration>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建 MeetingAnalyticsJob.scala**

```scala
package com.huadi.smm.spark

import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.apache.spark.sql.{DataFrame, SparkSession}

object MeetingAnalyticsJob {

  val JDBC_URL = "jdbc:mysql://localhost:3306/smart_meeting?useSSL=false&characterEncoding=utf-8&serverTimezone=Asia/Shanghai"
  val JDBC_USER = "root"
  val JDBC_PASS = sys.env.getOrElse("DB_PASSWORD", "1234")
  val FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("MeetingAnalytics")
      .config("spark.sql.shuffle.partitions", "4")
      .getOrCreate()

    import spark.implicits._

    val now = LocalDateTime.now().format(FMT)

    // 读取业务表
    val signinDF = readTable(spark, "sm_meeting_signin")
    val speechDF = readTable(spark, "sm_meeting_speech")
    val interactionDF = readTable(spark, "sm_meeting_interaction")
    val attendeeDF = readTable(spark, "meeting_attendee")
    val meetingDF = readTable(spark, "sm_meeting_info")
    val memberDF = readTable(spark, "sm_gm_members")

    // === 会议维度 ===
    val shouldAttendDF = attendeeDF.groupBy("meeting_id").count().withColumnRenamed("count", "should_attend")
    val signedDF = signinDF.groupBy("meeting_id").count().withColumnRenamed("count", "actual_attend")
    val normalDF = signinDF.where("sign_status = 0").groupBy("meeting_id").count().withColumnRenamed("count", "normal_count")
    val lateDF = signinDF.where("sign_status = 1").groupBy("meeting_id").count().withColumnRenamed("count", "late_count")
    val speechCntDF = speechDF.groupBy("meeting_id").count().withColumnRenamed("count", "speech_count")
    val interactionCntDF = interactionDF.groupBy("meeting_id").count().withColumnRenamed("count", "interaction_count")

    val meetingAnalytics = meetingDF
      .join(shouldAttendDF, Seq("meeting_id"), "left")
      .join(signedDF, Seq("meeting_id"), "left")
      .join(normalDF, Seq("meeting_id"), "left")
      .join(lateDF, Seq("meeting_id"), "left")
      .join(speechCntDF, Seq("meeting_id"), "left")
      .join(interactionCntDF, Seq("meeting_id"), "left")
      .na.fill(0)
      .selectExpr(
        "meeting_id",
        "meeting_title",
        "meeting_date",
        "cast(coalesce(should_attend, 0) as int) as should_attend",
        "cast(coalesce(actual_attend, 0) as int) as actual_attend",
        "cast(round(coalesce(actual_attend, 0) / greatest(coalesce(should_attend, 1), 1) * 100, 2) as decimal(5,2)) as attend_rate",
        "cast(coalesce(normal_count, 0) as int) as normal_count",
        "cast(coalesce(late_count, 0) as int) as late_count",
        "cast(coalesce(speech_count, 0) as int) as speech_count",
        "cast(coalesce(interaction_count, 0) as int) as interaction_count",
        s"cast(round(coalesce(actual_attend, 0) / greatest(coalesce(should_attend, 1), 1) * 0.4 + coalesce(speech_count, 0) / greatest(coalesce(should_attend, 1), 1) * 100 * 0.3 + coalesce(interaction_count, 0) / greatest(coalesce(should_attend, 1), 1) * 100 * 0.3, 2) as decimal(5,2)) as quality_score",
        s"'$now' as created_at",
        s"'$now' as updated_at"
      )
      .select("meeting_id", "meeting_title", "meeting_date", "should_attend", "actual_attend",
        "attend_rate", "normal_count", "late_count", "speech_count", "interaction_count",
        "quality_score", "created_at", "updated_at")

    writeTable(meetingAnalytics, "sm_meeting_analytics")

    // === 科室维度 ===
    val attendeeWithDept = attendeeDF
      .join(signinDF.select("meeting_id", "user_id"), Seq("meeting_id", "user_id"), "left")
      .join(memberDF.selectExpr("user_id", "department"), Seq("user_id"), "left")
      .join(meetingDF.select("meeting_id"), Seq("meeting_id"))

    val deptMeetingCnt = attendeeWithDept.select("department", "meeting_id").distinct()
      .groupBy("department").count().withColumnRenamed("count", "meeting_count")

    val deptAttendRate = attendeeWithDept
      .groupBy("department")
      .agg(
        org.apache.spark.sql.functions.count("*").as("total_attendee"),
        org.apache.spark.sql.functions.sum(org.apache.spark.sql.functions.when(
          org.apache.spark.sql.functions.col("user_id").isNotNull, 1).otherwise(0)).as("signed")
      )
      .selectExpr("department",
        "cast(round(signed / greatest(total_attendee, 1) * 100, 2) as decimal(5,2)) as avg_attend_rate")

    val speechWithDept = speechDF
      .join(memberDF.selectExpr("user_id as speaker_id2", "department"), $"speaker_id" === $"speaker_id2", "left")
      .groupBy("department").count().withColumnRenamed("count", "total_speech_count")

    val interactionWithDept = interactionDF
      .join(memberDF.selectExpr("user_id as from_user_id2", "department"), $"from_user_id" === $"from_user_id2", "left")
      .groupBy("department").count().withColumnRenamed("count", "total_interaction_count")

    val deptAnalytics = deptMeetingCnt
      .join(deptAttendRate, Seq("department"), "left")
      .join(speechWithDept, Seq("department"), "left")
      .join(interactionWithDept, Seq("department"), "left")
      .na.fill(0)
      .selectExpr(
        "department",
        "cast(coalesce(meeting_count, 0) as int) as meeting_count",
        "coalesce(avg_attend_rate, 0) as avg_attend_rate",
        "cast(coalesce(total_speech_count, 0) as int) as total_speech_count",
        "cast(coalesce(total_interaction_count, 0) as int) as total_interaction_count",
        s"'$now' as updated_at"
      )

    writeTable(deptAnalytics, "sm_department_analytics")

    // === 个人维度 ===
    val memberMeetings = attendeeDF.groupBy("user_id").count().withColumnRenamed("count", "total_meetings")
    val memberSigned = signinDF.groupBy("user_id").count().withColumnRenamed("count", "attended_count")
    val memberNormal = signinDF.where("sign_status = 0").groupBy("user_id").count().withColumnRenamed("count", "normal_count")
    val memberLate = signinDF.where("sign_status = 1").groupBy("user_id").count().withColumnRenamed("count", "late_count")
    val memberSpeech = speechDF.groupBy("speaker_id").count().withColumnRenamed("speaker_id", "uid").withColumnRenamed("count", "speech_count")
    val memberInteraction = interactionDF.groupBy("from_user_id").count().withColumnRenamed("from_user_id", "uid").withColumnRenamed("count", "interaction_count")

    val memberAnalytics = memberDF
      .selectExpr("user_id", "name as user_name", "department")
      .join(memberMeetings, Seq("user_id"), "left")
      .join(memberSigned, Seq("user_id"), "left")
      .join(memberNormal, Seq("user_id"), "left")
      .join(memberLate, Seq("user_id"), "left")
      .join(memberSpeech, memberDF("user_id") === memberSpeech("uid"), "left")
      .join(memberInteraction, memberDF("user_id") === memberInteraction("uid"), "left")
      .na.fill(0)
      .selectExpr(
        "user_id",
        "user_name",
        "department",
        "cast(coalesce(total_meetings, 0) as int) as total_meetings",
        "cast(coalesce(attended_count, 0) as int) as attended_count",
        "cast(round(coalesce(attended_count, 0) / greatest(coalesce(total_meetings, 1), 1) * 100, 2) as decimal(5,2)) as attend_rate",
        "cast(coalesce(normal_count, 0) as int) as normal_count",
        "cast(coalesce(late_count, 0) as int) as late_count",
        "cast(coalesce(speech_count, 0) as int) as speech_count",
        "cast(coalesce(interaction_count, 0) as int) as interaction_count",
        s"'$now' as last_updated"
      )

    writeTable(memberAnalytics, "sm_member_analytics")

    spark.stop()
  }

  def readTable(spark: SparkSession, table: String): DataFrame = {
    spark.read.format("jdbc")
      .option("url", JDBC_URL)
      .option("dbtable", table)
      .option("user", JDBC_USER)
      .option("password", JDBC_PASS)
      .load()
  }

  def writeTable(df: DataFrame, table: String): Unit = {
    df.write.format("jdbc")
      .option("url", JDBC_URL)
      .option("dbtable", table)
      .option("user", JDBC_USER)
      .option("password", JDBC_PASS)
      .mode("overwrite")
      .save()
  }
}
```

- [ ] **Step 3: 创建 submit.sh**

```bash
#!/bin/bash
# 提交 Spark 作业到集群
# 用法: bash submit.sh

/soft/spark-2.4.7/bin/spark-submit \
  --master spark://centos6:7077 \
  --class com.huadi.smm.spark.MeetingAnalyticsJob \
  --deploy-mode client \
  --executor-memory 512m \
  --total-executor-cores 2 \
  target/spark-analytics-1.0.jar
```

- [ ] **Step 4: 本地编译验证**

```bash
cd smart-report-interaction/spark && mvn compile
```

- [ ] **Step 5: 提交**

```bash
git add smart-report-interaction/spark/
git commit -m "feat: add Spark analytics job"
```

---

### Task 6: 创建前端 API 封装和路由

**Files:**
- Create: `smart-report-interaction/frontend/src/api/analytics.js`
- Modify: `smart-report-interaction/frontend/src/router/index.js`

- [ ] **Step 1: 创建 analytics.js**

```javascript
import request from './request'

export const getMeetingAnalytics = (meetingId) =>
  request.get(`/analytics/meeting/${meetingId}`)

export const getMeetingTrend = () =>
  request.get('/analytics/meetings/trend')

export const getDepartmentRanking = () =>
  request.get('/analytics/departments')

export const getMemberProfile = (userId) =>
  request.get(`/analytics/member/${userId}`)
```

- [ ] **Step 2: 更新路由，在 routes 数组里追加 analytics 路由**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/sign' },
  { path: '/sign', name: 'SignIn', component: () => import('../views/SignIn.vue') },
  { path: '/report', name: 'Report', component: () => import('../views/MeetingReport.vue') },
  { path: '/interaction', name: 'Interaction', component: () => import('../views/Interaction.vue') },
  { path: '/analytics', name: 'Analytics', component: () => import('../views/Analytics.vue') }
]

export default createRouter({ history: createWebHistory(), routes })
```

- [ ] **Step 3: 提交**

```bash
git add smart-report-interaction/frontend/src/api/analytics.js smart-report-interaction/frontend/src/router/index.js
git commit -m "feat: add analytics API module and route"
```

---

### Task 7: 创建 Vue 分析页面

**Files:**
- Create: `smart-report-interaction/frontend/src/views/Analytics.vue`

- [ ] **Step 1: 创建 Analytics.vue**

```vue
<template>
  <div class="page-layout">
    <div class="top-bar">
      <h2>数据分析</h2>
    </div>

    <div class="content">
      <!-- 会议趋势 -->
      <div class="panel">
        <h3>会议趋势</h3>
        <div ref="trendChart" style="width:100%;height:320px"></div>
      </div>

      <!-- 科室对比 -->
      <div class="panel">
        <h3>科室对比</h3>
        <div ref="deptChart" style="width:100%;height:320px"></div>
      </div>

      <!-- 科室排名表 -->
      <div class="panel">
        <h3>科室排名详情</h3>
        <el-table :data="departments" stripe style="width:100%">
          <el-table-column type="index" label="排名" width="60" />
          <el-table-column prop="department" label="科室" />
          <el-table-column prop="avgAttendRate" label="平均出勤率(%)" />
          <el-table-column prop="meetingCount" label="参会次数" />
          <el-table-column prop="totalSpeechCount" label="累计发言" />
          <el-table-column prop="totalInteractionCount" label="累计互动" />
        </el-table>
      </div>

      <!-- 会议列表 -->
      <div class="panel">
        <h3>会议评分明细</h3>
        <el-table :data="meetings" stripe style="width:100%">
          <el-table-column prop="meetingTitle" label="会议" />
          <el-table-column prop="meetingDate" label="日期" />
          <el-table-column prop="attendRate" label="出勤率(%)" />
          <el-table-column prop="speechCount" label="发言数" />
          <el-table-column prop="interactionCount" label="互动数" />
          <el-table-column prop="qualityScore" label="质量评分" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMeetingTrend, getDepartmentRanking } from '../api/analytics'

const trendChart = ref(null)
const deptChart = ref(null)
const departments = ref([])
const meetings = ref([])

onMounted(async () => {
  const [trendData, deptData] = await Promise.all([
    getMeetingTrend(),
    getDepartmentRanking()
  ])

  meetings.value = trendData.data || []
  departments.value = deptData.data || []

  await nextTick()
  renderTrendChart()
  renderDeptChart()
})

function renderTrendChart() {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  const list = meetings.value
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['出勤率', '发言数', '互动数'] },
    xAxis: { type: 'category', data: list.map(m => m.meetingDate || m.meetingTitle) },
    yAxis: [
      { type: 'value', name: '出勤率(%)', max: 100 },
      { type: 'value', name: '数量' }
    ],
    series: [
      { name: '出勤率', type: 'line', data: list.map(m => m.attendRate), smooth: true },
      { name: '发言数', type: 'line', yAxisIndex: 1, data: list.map(m => m.speechCount), smooth: true },
      { name: '互动数', type: 'line', yAxisIndex: 1, data: list.map(m => m.interactionCount), smooth: true }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderDeptChart() {
  if (!deptChart.value) return
  const chart = echarts.init(deptChart.value)
  const list = departments.value
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: list.map(d => d.department) },
    yAxis: { type: 'value', name: '出勤率(%)', max: 100 },
    series: [{
      name: '平均出勤率', type: 'bar', data: list.map(d => d.avgAttendRate),
      itemStyle: { color: '#2563EB' }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}
</script>
```

- [ ] **Step 2: 提交**

```bash
git add smart-report-interaction/frontend/src/views/Analytics.vue
git commit -m "feat: add Analytics vue page"
```

---

### Task 8: 集成验证

- [ ] **Step 1: 启动后端验证编译**

```bash
cd smart-report-interaction/backend && ./mvnw compile -q
```

预期：BUILD SUCCESS。

- [ ] **Step 2: 启动前端验证编译**

```bash
cd smart-report-interaction/frontend && npx vite build
```

预期：无错误，构建成功。

- [ ] **Step 3: 验证 API 可访问（插入测试数据后）**

```bash
curl -s http://localhost:8081/api/analytics/meetings/trend
```

预期：返回 JSON，`success: true`。

- [ ] **Step 4: Playwright 浏览器验证前端页面**

打开 `http://localhost:5174/analytics`，确认页面渲染正常、图表显示、表格数据正常。

- [ ] **Step 5: 提交**

```bash
git add smart-report-interaction/
git commit -m "chore: final integration verification for analytics module"
```

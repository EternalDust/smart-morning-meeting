"""Generate 6 months of morning meeting data for big data analytics."""
import random
import mysql.connector
from datetime import datetime, timedelta

random.seed(42)

DB = {
    'host': 'localhost', 'port': 3306, 'user': 'root',
    'password': '1234', 'database': 'smart_meeting', 'charset': 'utf8mb4'
}

DEPT_NAMES = ['外科', '内科', '儿科', '妇产科', '骨科', '急诊科', '影像科', '麻醉科', '护理部']
DEPT_IDS = list(range(1, 10))

MEMBERS = [
    ('1001', '张建国', '外科'), ('1002', '李明辉', '外科'), ('1003', '王芳', '内科'),
    ('1004', '刘晓东', '儿科'), ('1005', '陈丽华', '护理部'), ('1006', '周建军', '骨科'),
    ('1007', '赵敏', '急诊科'), ('1008', '郑伟', '影像科'), ('1009', '吴志强', '妇产科'),
    ('1010', '郑丽华', '内科'), ('1011', '孙志远', '护理部'), ('1012', '黄晓明', '麻醉科'),
]

HOSTS = [
    ('2001', '杨辉', '管理层'), ('2002', '王建国', '管理层'), ('2003', '李秀英', '管理层'),
]

ALL_MEMBERS = MEMBERS + HOSTS

TITLES = [
    '周一科室晨会', '周二病例讨论会', '周三质控分析会', '周四教学查房会',
    '周五科室总结会', '月度医疗质量会', '季度绩效考核会', '年度工作总结会',
    '科室安全例会', '护理质量分析会', '感染防控专题会', '医保政策培训会',
    '新技术推广会', '疑难病例讨论会', '医疗纠纷预防会', '科研进展汇报会',
    '药品使用分析会', '绩效考核沟通会', '患者满意度分析会', '医疗设备管理会',
]

CONTENTS = [
    '本月门诊量{d}人次，同比增长{r}%，其中专家门诊增长{r2}%',
    '外科手术量{d}台，其中三级以上手术{d2}台，占比{r}%',
    '急诊接诊量{d}人次，抢救成功率{r}%',
    '护理部完成质控检查{d}项，合格率{r}%',
    '影像科完成检查{d}例，阳性率{r}%',
    '药占比控制在{r}%，较上月下降{d}个百分点',
    '患者满意度评分{d}分，投诉量减少{r}%',
    '本月抗生素使用率{r}%，达标',
    '床位周转率{r}%，平均住院日{d}天',
    '院内感染率控制在{r}%，低于国家标准',
    '门诊处方合格率{r}%，较上月提高{d}%',
    '新项目开展{d}项，已通过评审',
]

INTERACTIONS = [
    ('提问', ['外科手术量增长的具体原因是什么？', '能否提供各科室门诊数据的详细拆分？', '护理人员排班是否需要调整？', '新设备的采购进度如何？', '抗生素使用标准是否有更新？']),
    ('反馈', ['建议按科室拆分门诊数据，便于对照分析', '当前排班制度需要优化', '建议增加周六专家门诊', '医疗废物处理流程需要改进', '建议加强科室间协作机制']),
    ('投票', ['下月是否将晨会时间调整为08:00', '是否增加周末门诊', '是否引入新的电子病历系统', '是否调整绩效考核方案', '是否开展患者满意度专项调查']),
]

def generate_date(i):
    base = datetime(2026, 1, 5)  # First Monday of 2026
    day = base + timedelta(days=i)
    while day.weekday() >= 5:  # Skip weekends
        day = day + timedelta(days=1)
    return day

def generate(conn, start_id=100):
    cursor = conn.cursor()
    mid = start_id

    for i in range(120):
        day = generate_date(i)
        is_morning = i % 2 == 0
        start_time = f'{day.strftime("%Y-%m-%d")} {"08:30:00" if is_morning else "17:00:00"}'
        end_time = f'{day.strftime("%Y-%m-%d")} {"09:30:00" if is_morning else "18:00:00"}'
        title = random.choice(TITLES)
        status = 2  # completed
        location = f'会议室{random.choice("ABCD")}'

        cursor.execute(
            'INSERT INTO sm_meeting_info (title, meeting_type, dept_id, host_id, start_time, end_time, location, status, approve_status, creator_id, create_time, update_time) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
            (title, 1, random.choice(DEPT_IDS), 2001, start_time, end_time, location, status, 2, 2001, start_time, end_time)
        )
        meeting_id = mid

        # Attendees: 12-20 random members + at least 1 host
        n_attend = random.randint(12, 20)
        picked = random.sample(ALL_MEMBERS, min(n_attend, len(ALL_MEMBERS)))
        for uid, name, dept in picked:
            cursor.execute(
                'INSERT INTO sm_meeting_attendee (meeting_id, user_id, role_type, attend_status, invite_time) VALUES (%s,%s,%s,%s,%s)',
                (meeting_id, uid, 2 if uid.startswith('1') else 1, 0, start_time)
            )

        # Sign-ins: 60-90% of picked members sign in
        sign_count = int(len(picked) * random.uniform(0.6, 0.9))
        signers = random.sample(picked, sign_count)
        for uid, name, dept in signers:
            href = start_time[:10]
            if is_morning:
                sign_status = 0 if random.random() < 0.7 else 1  # 70% on time
                hour, minute = (8, random.randint(20, 35)) if sign_status == 0 else (8, random.randint(31, 55))
            else:
                sign_status = 0 if random.random() < 0.5 else 1  # 50% on time
                hour, minute = (17, random.randint(0, 10)) if sign_status == 0 else (17, random.randint(11, 30))
            sign_time = f'{href} {hour:02d}:{minute:02d}:{random.randint(0,59):02d}'
            cursor.execute(
                'INSERT INTO sm_meeting_signin (meeting_id, user_id, sign_time, sign_type, sign_status) VALUES (%s,%s,%s,%s,%s)',
                (meeting_id, uid, sign_time, 2, sign_status)
            )

        # Speeches: 2-5 random speakers
        speech_count = random.randint(2, 5)
        speakers = random.sample(signers, min(speech_count, len(signers)))
        for uid, name, dept in speakers:
            tmpl = random.choice(CONTENTS)
            content = tmpl.format(
                d=random.randint(20, 500), d2=random.randint(1, 50),
                r=round(random.uniform(0.5, 15), 1), r2=round(random.uniform(0.5, 20), 1)
            )
            speech_time = f'{href} {random.randint(8,9):02d}:{random.randint(20,55):02d}:{random.randint(0,59):02d}'
            cursor.execute(
                'INSERT INTO sm_meeting_speech (meeting_id, speaker_id, content, speech_time) VALUES (%s,%s,%s,%s)',
                (meeting_id, uid, content, speech_time)
            )

        # Interactions: 1-4 random
        inter_count = random.randint(1, 4)
        inter_users = random.sample(signers, min(inter_count, len(signers)))
        for uid, name, dept in inter_users:
            itype_str, options = random.choice(INTERACTIONS)
            itype = {'提问': 1, '反馈': 2, '投票': 3}[itype_str]
            content = random.choice(options)
            create_time = f'{href} {random.randint(8,9):02d}:{random.randint(30,50):02d}:{random.randint(0,59):02d}'
            cursor.execute(
                'INSERT INTO sm_meeting_interaction (meeting_id, user_id, interact_type, content, create_time) VALUES (%s,%s,%s,%s,%s)',
                (meeting_id, uid, itype, content, create_time)
            )

        mid += 1
        if i % 20 == 0:
            conn.commit()
            print(f'  {i}/120 meetings generated...')

    conn.commit()
    cursor.close()
    return mid - start_id

if __name__ == '__main__':
    conn = mysql.connector.connect(**DB)
    print('Generating 120 meetings (6 months)...')
    total = generate(conn, start_id=100)
    print(f'Done: {total} meetings created')
    conn.close()

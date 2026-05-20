import json
import time
import random
from kafka import KafkaProducer

def get_kafka_producer(brokers='localhost:9092'):
    """初始化 Kafka Producer"""
    producer = KafkaProducer(
        bootstrap_servers=[brokers],
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )
    return producer

def generate_mock_data():
    """
    模拟离线数据与实时数据的收集:
    晨会数据、督办数据、医疗业务数据。
    """
    departments = ['Cardiology', 'Neurology', 'Pediatrics', 'Emergency', 'Surgery']
    # 偶尔注入异常数据以触发预警
    inject_anomaly = random.random() < 0.15  # 15% 概率产生异常

    dept = random.choice(departments)
    dept_id_map = {'Cardiology': 1, 'Neurology': 2, 'Pediatrics': 3, 'Emergency': 4, 'Surgery': 5}

    # 模拟参会应到与实到人数
    expected_attendees = random.randint(10, 50)
    if inject_anomaly:
        actual_attendees = random.randint(0, int(expected_attendees * 0.5))  # 参会率骤降
    else:
        actual_attendees = random.randint(int(expected_attendees * 0.7), expected_attendees)

    # 模拟督办任务和解决数
    total_issues = random.randint(5, 20)
    if inject_anomaly:
        resolved_issues = random.randint(0, int(total_issues * 0.4))  # 解决率偏低
    else:
        resolved_issues = random.randint(int(total_issues * 0.6), total_issues)

    data = {
        "timestamp": int(time.time()),
        "department": dept,
        "dept_id": dept_id_map[dept],
        "expected_attendees": expected_attendees,
        "actual_attendees": actual_attendees,
        "total_issues": total_issues,
        "resolved_issues": resolved_issues,
        "late_num": random.randint(0, 8),
        "overdue_count": random.randint(0, 15) if inject_anomaly else random.randint(0, 3),
        "urgent_count": random.randint(0, 5),
        "avg_duration": random.randint(10, 40) if not inject_anomaly else random.randint(1, 60),
    }
    return data

if __name__ == "__main__":
    KAFKA_TOPIC = "morning_meeting_topic"
    print(f"开始向 Kafka Topic '{KAFKA_TOPIC}' 发送模拟数据...")
    producer = get_kafka_producer()

    try:
        while True:
            data = generate_mock_data()
            producer.send(KAFKA_TOPIC, data)
            print(f"Sent: {data}")
            time.sleep(2) # 模拟每2秒产生一条数据
    except KeyboardInterrupt:
        print("停止发送数据")
    finally:
        producer.close()

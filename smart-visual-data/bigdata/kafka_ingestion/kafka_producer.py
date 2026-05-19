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
    dept = random.choice(departments)
    
    # 模拟参会应到与实到人数
    expected_attendees = random.randint(10, 50)
    actual_attendees = random.randint(int(expected_attendees * 0.7), expected_attendees)
    
    # 模拟督办任务和解决数
    total_issues = random.randint(5, 20)
    resolved_issues = random.randint(0, total_issues)

    data = {
        "timestamp": int(time.time()),
        "department": dept,
        "expected_attendees": expected_attendees,
        "actual_attendees": actual_attendees,
        "total_issues": total_issues,
        "resolved_issues": resolved_issues
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

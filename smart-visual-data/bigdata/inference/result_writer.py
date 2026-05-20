"""
结果写入模块
功能：将预警记录写入 MySQL bi_warn_record 表和 Redis 缓存
"""
import json
import pymysql
import redis

from models.config import (
    MYSQL_HOST, MYSQL_PORT, MYSQL_DB, MYSQL_USER, MYSQL_PASSWORD,
    REDIS_HOST, REDIS_PORT, REDIS_DB, REDIS_WARN_KEY, REDIS_MAX_CACHED,
)


class ResultWriter:
    def __init__(self):
        self._mysql_conn = None
        self._redis_client = None

    # ── MySQL ──

    @property
    def mysql(self):
        if self._mysql_conn is None:
            self._mysql_conn = pymysql.connect(
                host=MYSQL_HOST,
                port=MYSQL_PORT,
                database=MYSQL_DB,
                user=MYSQL_USER,
                password=MYSQL_PASSWORD,
                charset="utf8mb4",
            )
        return self._mysql_conn

    def write_to_mysql(self, record: dict):
        """
        INSERT INTO bi_warn_record
        字段映射：warn_type, warn_level, dept_id, index_code,
                 abnormal_value, threshold_value, status, create_time
        """
        if record is None:
            return

        sql = """
            INSERT INTO bi_warn_record
                (warn_type, warn_level, dept_id, index_code,
                 abnormal_value, threshold_value, status, create_time)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """
        params = (
            record["warn_type"],
            record["warn_level"],
            record.get("dept_id", 0),
            record["index_code"],
            record["abnormal_value"],
            record["threshold_value"],
            record["status"],
            record["create_time"],
        )
        with self.mysql.cursor() as cursor:
            cursor.execute(sql, params)
        self.mysql.commit()

    # ── Redis ──

    @property
    def redis_client(self):
        if self._redis_client is None:
            self._redis_client = redis.Redis(
                host=REDIS_HOST, port=REDIS_PORT, db=REDIS_DB, decode_responses=True
            )
        return self._redis_client

    def write_to_redis(self, record: dict):
        """将最新预警推入 Redis 列表，保留最近 N 条"""
        if record is None:
            return
        self.redis_client.lpush(REDIS_WARN_KEY, json.dumps(record, ensure_ascii=False))
        self.redis_client.ltrim(REDIS_WARN_KEY, 0, REDIS_MAX_CACHED - 1)

    def write(self, record: dict):
        """写入 MySQL + Redis"""
        self.write_to_mysql(record)
        self.write_to_redis(record)

    def close(self):
        if self._mysql_conn:
            self._mysql_conn.close()
        if self._redis_client:
            self._redis_client.close()

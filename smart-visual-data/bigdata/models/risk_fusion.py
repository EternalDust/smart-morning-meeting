"""
风险融合决策模块
功能：融合 IF + LSTM 检测结果，按三级风险分级，生成预警记录
"""
import numpy as np
from datetime import datetime

from .config import IF_WEIGHT, LSTM_WEIGHT, RISK_THRESHOLDS, WARN_TYPES


def _determine_warn_type(features: dict, raw_data: dict) -> str:
    """
    根据特征值和原始数据判断最可能的预警类型。
    返回 WARN_TYPES 中的 key。
    """
    attend_rate = features.get("attend_rate", 100)
    solve_rate = features.get("solve_rate", 100)
    overdue_count = features.get("overdue_count", 0)
    absent_num = features.get("absent_num", 0)
    avg_duration = features.get("avg_duration", 0)

    if attend_rate < 80:
        return "ATTEND_DROP"
    if solve_rate < 70:
        return "SOLVE_DROP"
    if overdue_count > 10:
        return "OVERDUE_SURGE"
    if absent_num > 10:
        return "ABSENT_SURGE"
    if avg_duration < 5 or avg_duration > 60:
        return "DURATION_ABNORMAL"
    return "COMPLETE_DROP"


def _get_threshold_description(warn_type: str) -> str:
    """根据预警类型返回可读的阈值描述"""
    thresholds = {
        "ATTEND_DROP": "参会率 < 80%",
        "SOLVE_DROP": "解决率 < 70%",
        "OVERDUE_SURGE": "逾期数 > 10",
        "ABSENT_SURGE": "缺席人数 > 10",
        "DURATION_ABNORMAL": "时长 < 5min 或 > 60min",
        "COMPLETE_DROP": "完成度异常下降",
    }
    return thresholds.get(warn_type, "异常阈值触发")


def fuse(
    if_score: float,
    lstm_score: float,
    if_is_anomaly: bool,
    lstm_is_anomaly: bool,
    features: dict,
    raw_data: dict,
) -> dict:
    """
    融合 IF 和 LSTM 的检测结果，输出最终预警记录。

    参数:
      if_score, lstm_score: 各自异常得分 [0,1]
      if_is_anomaly, lstm_is_anomaly: 各自是否标记为异常
      features: 特征字典（含原始特征值）
      raw_data: 原始数据 (需要有 department / dept_id 等信息)

    返回结构化预警字典:
      {
        "warn_type": "ATTEND_DROP",
        "warn_level": 2,             # 1-一般 2-中等 3-重大
        "anomaly_score": 0.78,
        "abnormal_value": "78.5",
        "threshold_value": "参会率<80%",
        "detail": "...",
        "dept_name": "心内科",
        "create_time": "2026-05-19 08:30:00",
        "status": 0,
      }
    """
    # 1. 融合得分
    fused_score = IF_WEIGHT * if_score + LSTM_WEIGHT * lstm_score

    # 2. 是否判定为异常（任意一个检测到即触发）
    is_anomaly = if_is_anomaly or lstm_is_anomaly
    if not is_anomaly:
        return None  # 非异常不产生预警

    # 3. 三级风险分级
    warn_level = 1  # 默认一般
    if fused_score >= RISK_THRESHOLDS["level_3"]["min_score"]:
        warn_level = 3
    elif fused_score >= RISK_THRESHOLDS["level_2"]["min_score"]:
        warn_level = 2

    # 4. 确定预警类型
    warn_type = _determine_warn_type(features, raw_data)

    # 5. 构造异常值描述（取最异常的指标）
    abnormal_value = ""
    if warn_type == "ATTEND_DROP":
        abnormal_value = f"{features.get('attend_rate', 'N/A')}%"
    elif warn_type == "SOLVE_DROP":
        abnormal_value = f"{features.get('solve_rate', 'N/A')}%"
    elif warn_type == "OVERDUE_SURGE":
        abnormal_value = f"{features.get('overdue_count', 'N/A')}件"
    elif warn_type == "ABSENT_SURGE":
        abnormal_value = f"{features.get('absent_num', 'N/A')}人"
    elif warn_type == "DURATION_ABNORMAL":
        abnormal_value = f"{features.get('avg_duration', 'N/A')}分钟"

    # 6. 生成详细说明
    dept_name = raw_data.get("dept_name", raw_data.get("department", "未知科室"))
    level_name = {1: "一般", 2: "中等", 3: "重大"}.get(warn_level, "一般")
    detail = (
        f"{dept_name}触发{level_name}预警[{WARN_TYPES.get(warn_type, warn_type)}]："
        f"异常值={abnormal_value}，"
        f"阈值={_get_threshold_description(warn_type)}，"
        f"融合异常得分={fused_score:.2f}"
    )

    return {
        "warn_type": warn_type,
        "warn_level": warn_level,
        "anomaly_score": round(fused_score, 4),
        "abnormal_value": abnormal_value,
        "threshold_value": _get_threshold_description(warn_type),
        "detail": detail,
        "dept_name": dept_name,
        "dept_id": raw_data.get("dept_id", 0),
        "index_code": warn_type,
        "create_time": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "status": 0,  # 0=未处理
    }

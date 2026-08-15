#!/bin/bash
# Submit Spark job to cluster
# Usage: bash submit.sh

/soft/spark-2.4.7/bin/spark-submit \
  --master spark://centos6:7077 \
  --class com.huadi.smm.spark.MeetingAnalyticsJob \
  --deploy-mode client \
  --executor-memory 512m \
  --total-executor-cores 2 \
  target/spark-analytics-1.0.jar

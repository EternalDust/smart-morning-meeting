package com.huadi.smm.workbench.service.impl;

import com.huadi.smm.workbench.service.DataLineageService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataLineageServiceImpl implements DataLineageService {

    /**
     * 获取表级数据血缘
     * 完整实现中集成Apache Atlas的Hook机制自动捕获输入输出依赖
     */
    @Override
    public Map<String, Object> getTableLineage(String tableName) {
        Map<String, Object> lineage = new HashMap<>();
        lineage.put("tableName", tableName);
        lineage.put("level", "table");

        // 上游表——数据来源
        List<Map<String, String>> upstream = new ArrayList<>();
        upstream.add(lineageNode("ods_raw_data", "原始数据层", "Kafka -> Flink -> ODS"));
        upstream.add(lineageNode("data_source_config", "数据源配置", "MySQL"));
        lineage.put("upstream", upstream);

        // 下游表——数据去向
        List<Map<String, String>> downstream = new ArrayList<>();
        downstream.add(lineageNode("dwd_clean_data", "明细数据层", "Spark ETL"));
        downstream.add(lineageNode("dws_agg_indicators", "汇总指标层", "Spark Aggregation"));
        downstream.add(lineageNode("ads_quality_report", "应用数据层", "BI报表"));
        lineage.put("downstream", downstream);

        return lineage;
    }

    @Override
    public Map<String, Object> getRecordLineage(String tableName, Long recordId) {
        Map<String, Object> lineage = getTableLineage(tableName);
        lineage.put("level", "record");
        lineage.put("recordId", recordId);
        lineage.put("transformSteps", Arrays.asList(
                "① 原始数据采集 (Kafka offset: 1523401)",
                "② 数据去重 (matched by: patient_id + visit_time)",
                "③ 格式标准化 (date -> yyyy-MM-dd HH:mm:ss, gender -> 0/1)",
                "④ 空值填充 (age: null -> -1)",
                "⑤ 质量评分 (score: 87.5, level: 高)",
                "⑥ 写入清洗后数据表 (dwd_clean_data)"
        ));
        return lineage;
    }

    @Override
    public Map<String, Object> getLineageGraph(String level) {
        Map<String, Object> graph = new HashMap<>();
        graph.put("level", level);

        // 返回DAG图数据，前端使用 ECharts/G6 渲染
        List<Map<String, Object>> nodes = new ArrayList<>();
        nodes.add(graphNode("ods_raw_data", "原始数据", "source"));
        nodes.add(graphNode("kafka_raw_topic", "Kafka缓冲", "middleware"));
        nodes.add(graphNode("flink_realtime_job", "Flink实时处理", "compute"));
        nodes.add(graphNode("dwd_clean_data", "清洗后数据", "target"));
        nodes.add(graphNode("spark_batch_job", "Spark批处理", "compute"));
        nodes.add(graphNode("dws_agg_indicators", "汇总指标", "target"));

        List<Map<String, Object>> edges = new ArrayList<>();
        edges.add(graphEdge("ods_raw_data", "kafka_raw_topic", "CDC同步"));
        edges.add(graphEdge("kafka_raw_topic", "flink_realtime_job", "实时消费"));
        edges.add(graphEdge("flink_realtime_job", "dwd_clean_data", "写入HBase"));
        edges.add(graphEdge("dwd_clean_data", "spark_batch_job", "批量处理"));
        edges.add(graphEdge("spark_batch_job", "dws_agg_indicators", "聚合写入Hive"));

        graph.put("nodes", nodes);
        graph.put("edges", edges);
        return graph;
    }

    private Map<String, String> lineageNode(String table, String desc, String process) {
        Map<String, String> node = new LinkedHashMap<>();
        node.put("table", table);
        node.put("description", desc);
        node.put("process", process);
        return node;
    }

    private Map<String, Object> graphNode(String id, String label, String type) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", id);
        node.put("label", label);
        node.put("type", type);
        return node;
    }

    private Map<String, Object> graphEdge(String source, String target, String label) {
        Map<String, Object> edge = new HashMap<>();
        edge.put("source", source);
        edge.put("target", target);
        edge.put("label", label);
        return edge;
    }
}

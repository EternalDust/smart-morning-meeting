package com.huadi.smm.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huadi.smm.dao.ApproveTaskMapper;
import com.huadi.smm.dao.MeetingInfoMapper;
import com.huadi.smm.entity.ApproveProcessDef;
import com.huadi.smm.entity.ApproveTask;
import com.huadi.smm.entity.MeetingInfo;
import com.huadi.smm.entity.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApproveFlowEngine {

    @Autowired
    private ApproveProcessDefService processDefService;
    @Autowired
    private ApproveTaskMapper taskMapper;
    @Autowired
    private MeetingInfoMapper meetingMapper;

    public void start(Long meetingId, Long processDefId) {
        ApproveProcessDef def = processDefService.getById(processDefId);
        if (def == null || def.getNodesJson() == null) return;
        List<Node> nodes = JSON.parseArray(def.getNodesJson(), Node.class);
        Map<String, Node> map = nodes.stream().collect(Collectors.toMap(Node::getNodeId, n -> n));
        Node start = nodes.stream().filter(n -> "start".equals(n.getNodeType())).findFirst().orElse(null);
        if (start == null) return;

        MeetingInfo m = meetingMapper.selectById(meetingId);
        m.setApproveStatus(1);
        m.setProcessId(processDefId);
        meetingMapper.updateById(m);
        enterNode(meetingId, start.getNextNodeId(), map);
    }

    private void enterNode(Long meetingId, String nodeId, Map<String, Node> map) {
        Node node = map.get(nodeId);
        if (node == null) return;
        if ("end".equals(node.getNodeType())) {
            MeetingInfo m = meetingMapper.selectById(meetingId);
            m.setApproveStatus(2);
            meetingMapper.updateById(m);
            return;
        }
        for (Long approverId : node.getApproverIds()) {
            ApproveTask t = new ApproveTask();
            t.setMeetingId(meetingId);
            t.setNodeId(nodeId);
            t.setNodeType(node.getNodeType());
            t.setApproverId(approverId);
            t.setStatus(0);
            taskMapper.insert(t);
        }
    }

    public void handle(Long taskId, Integer action, String opinion) {
        ApproveTask task = taskMapper.selectById(taskId);
        if (task == null || task.getStatus() != 0) return;
        task.setAction(action);
        task.setOpinion(opinion);
        task.setStatus(action == 1 ? 1 : 2);
        task.setApproveTime(new Date());
        taskMapper.updateById(task);

        Long meetingId = task.getMeetingId();
        if (action == 2) {
            MeetingInfo m = meetingMapper.selectById(meetingId);
            m.setApproveStatus(3);
            meetingMapper.updateById(m);
            return;
        }

        List<ApproveTask> list = taskMapper.selectList(
                Wrappers.<ApproveTask>lambdaQuery()
                        .eq(ApproveTask::getMeetingId, meetingId)
                        .eq(ApproveTask::getNodeId, task.getNodeId()));
        boolean done = "serial".equals(task.getNodeType())
                ? list.stream().allMatch(t -> t.getStatus() == 1)
                : list.stream().anyMatch(t -> t.getStatus() == 1);

        if (done) {
            MeetingInfo m = meetingMapper.selectById(meetingId);
            ApproveProcessDef def = processDefService.getById(m.getProcessId());
            List<Node> nodes = JSON.parseArray(def.getNodesJson(), Node.class);
            Map<String, Node> map = nodes.stream().collect(Collectors.toMap(Node::getNodeId, n -> n));
            Node current = map.get(task.getNodeId());
            enterNode(meetingId, current.getNextNodeId(), map);
        }
    }
}
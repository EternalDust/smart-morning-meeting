package com.huadi.smm.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class Node implements Serializable {
    private String nodeId;
    private String nodeType;
    private List<Long> approverIds;
    private String nextNodeId;
}
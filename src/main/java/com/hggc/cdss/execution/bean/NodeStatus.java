package com.hggc.cdss.execution.bean;

public class NodeStatus {

    private int id;
    private int nodeId;
    private String nodeName;
    private String nodeStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(String nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    @Override
    public String toString() {
        return "NodeStatus{" +
                "id=" + id +
                ", nodeId=" + nodeId +
                ", nodeName='" + nodeName + '\'' +
                ", nodeStatus='" + nodeStatus + '\'' +
                '}';
    }
}

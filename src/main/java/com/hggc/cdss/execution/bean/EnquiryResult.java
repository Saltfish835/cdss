package com.hggc.cdss.execution.bean;

public class EnquiryResult {

    private int id;
    private String sourceName;//这个问题的名字
    private String result;//如果是多选，多个选项之间用逗号连接
    private String sourceCaption;//这个问题的标题
    private String selectModel;//这个问题是单选还是多选
    private String dataType;//这个问题的数据类型
    private int nodeId;//这个问题属于哪一个任务

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSourceCaption() {
        return sourceCaption;
    }

    public void setSourceCaption(String sourceCaption) {
        this.sourceCaption = sourceCaption;
    }

    public String getSelectModel() {
        return selectModel;
    }

    public void setSelectModel(String selectModel) {
        this.selectModel = selectModel;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "EnquiryResult{" +
                "id=" + id +
                ", sourceName='" + sourceName + '\'' +
                ", result='" + result + '\'' +
                ", sourceCaption='" + sourceCaption + '\'' +
                ", selectModel='" + selectModel + '\'' +
                ", dataType='" + dataType + '\'' +
                ", nodeId=" + nodeId +
                '}';
    }
}

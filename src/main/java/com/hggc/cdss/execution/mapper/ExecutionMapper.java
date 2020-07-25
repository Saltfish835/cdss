package com.hggc.cdss.execution.mapper;

import com.hggc.cdss.execution.bean.DecisionResult;
import com.hggc.cdss.execution.bean.EnquiryResult;
import com.hggc.cdss.execution.bean.NodeStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface ExecutionMapper {

    /**
     * 查看节点状态表中有多少条记录
     * @return
     */
    @Select("select count(*) from node_status")
    public int getRecordNumberOfNodeStatus();


    /**
     * 删除节点状态表的所有数据
     */
    @Delete("delete from node_status")
    public void deleteAllRecordOdNodeStatus();


    /**
     * 向节点状态表中插入一条数据
     * @param nodeStatus
     */
    @Insert("insert into node_status(node_id,node_name,node_status) values(#{nodeId},#{nodeName},#{nodeStatus})")
    public void addDataToNodeStatus(NodeStatus nodeStatus);


    /**
     * 查看问询结果表中有多少条记录
     * @return
     */
    @Select("select count(*) from enquiry_result")
    public int getRecordNumberOfEnquiryResult();


    /**
     * 删除问询结果表中所有的数据
     */
    @Delete("delete from enquiry_result")
    public void deleteAllRecordOfEnquiryResult();


    /**
     * 更新对应节点的状态
     * @param nodeStatus
     */
    @Update("update node_status set node_status = #{nodeStatus} where node_id = #{nodeId}")
    public void updateStatusOfNode(NodeStatus nodeStatus);


    /**
     * 向enquiry_result表中添加数据
     * @param enquiryResult
     */
    @Insert("insert into enquiry_result(source_name,result,source_caption,select_model,data_type,node_id) " +
            "values(#{sourceName},#{result},#{sourceCaption},#{selectModel},#{dataType},#{nodeId})")
    public void addDataToEnquiryResult(EnquiryResult enquiryResult);


    /**
     * 通过nodeId查询这个节点的状态
     * @param nodeId
     * @return
     */
    @Select("select node_status from node_status where node_id = #{nodeId}")
    public String getNodeStatusByNodeId(int nodeId);


    /**
     * 查询问题结果表中所有的数据，并封装成对象
     * @return
     */
    @Results(id="enquiryResultList",value={
            @Result(id=true,column = "id",property = "id"),
            @Result(column = "source_name",property = "sourceName"),
            @Result(column = "result",property = "result"),
            @Result(column = "source_caption",property = "sourceCaption"),
            @Result(column = "select_model",property = "selectModel"),
            @Result(column = "data_type",property = "dataType"),
            @Result(column = "node_id",property = "nodeId")
    })
    @Select("select * from enquiry_result")
    public List<EnquiryResult> selectAllFromEnquiryResult();


    /**
     * 向决策结果表中插入数据
     * @param decisionResult
     */
    @Insert("insert into decision_result(decision_name,node_id,candidate_name) values(#{decisionName},#{nodeId},#{candidateName})")
    public void addDataToDecisionResult(DecisionResult decisionResult);


    /**
     * 删除决策结果表中全部数据
     */
    @Delete("delete from decision_result")
    public void deleteAllRecordOfDecisionResult();


    /**
     * 得到所有的决策结果，并封装成对象
     * @return
     */
    @Results(id="decisionResultList",value = {
            @Result(id=true,column = "id",property = "id"),
            @Result(column = "decision_name",property = "decisionName"),
            @Result(column = "node_id",property = "nodeId"),
            @Result(column = "candidate_name",property = "candidateName")
    })
    @Select("select * from decision_result")
    public List<DecisionResult> selectAddFromDecisionResult();





    @Results(id="nodeStatusResultList",value = {
            @Result(id=true,column="id",property = "id"),
            @Result(column = "node_id",property = "nodeId"),
            @Result(column = "node_name",property = "nodeName"),
            @Result(column = "node_status",property = "nodeStatus")
    })
    @Select("select * from node_status")
    public List<NodeStatus> selectAllFromNodeStatusResult();


}

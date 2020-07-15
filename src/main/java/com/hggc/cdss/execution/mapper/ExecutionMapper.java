package com.hggc.cdss.execution.mapper;

import com.hggc.cdss.execution.bean.NodeStatus;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


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

}

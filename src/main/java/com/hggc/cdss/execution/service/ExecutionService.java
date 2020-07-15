package com.hggc.cdss.execution.service;

import com.hggc.cdss.execution.bean.Graph;
import com.hggc.cdss.execution.bean.NodeStatus;
import com.hggc.cdss.execution.component.Loader;
import com.hggc.cdss.execution.mapper.ExecutionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 这个就是调度器
 */
@Service
public class ExecutionService {

    @Autowired
    Loader loader;

    @Resource
    ExecutionMapper executionMapper;

    private Graph graph = null;

    public Graph getGraph() {
        return this.graph;
    }

    /**
     * 初始化任务网络执行的环境
     *
     */
    public void initEnv(String diseaseName) throws Exception{
        Map guideline = loader.getGuideline(diseaseName);//先获得整个指南
        //从指南中获得所有节点,也就是任务网络中的节点
        List<Map<String,Object>> taskList = (List<Map<String,Object>>)guideline.get("task_table");
        //判断节点状态表中有没有数据
        int num = executionMapper.getRecordNumberOfNodeStatus();
        if(num > 0) {//说明节点状态表中有数据
            //删除掉表中的全部数据
            executionMapper.deleteAllRecordOdNodeStatus();
        }
        //遍历所有节点
        for(Map<String,Object> node : taskList) {
            NodeStatus nodeStatus = new NodeStatus();
            nodeStatus.setNodeId((int)node.get("nodeId"));
            nodeStatus.setNodeName((String)node.get("name"));
            nodeStatus.setNodeStatus("dormant");//初始情况下所有的任务都是休眠状态
            //将节点添加到节点信息表中
            executionMapper.addDataToNodeStatus(nodeStatus);
        }
        //判断存储问询结果表中有没有数据
        num = executionMapper.getRecordNumberOfEnquiryResult();
        if(num > 0) {//说明问询结果表中有数据
            //删掉里面所有的数据
            executionMapper.deleteAllRecordOfEnquiryResult();
        }
        graph =  loader.getGraph(diseaseName);//最开始为这个graph赋值
    }


    /**
     * 得到最开始的任务，也就是第一个任务
     * @return
     */
    public Map<String,Object> getFristTask() throws Exception{
        //从graph中获取图的起点
        Map<String,Object> startingPoint = (Map<String,Object>)graph.getStaertingPoint();
        //判断起点的前置条件
        if(!((String)startingPoint.get("precondition")).equals("")) {
            throw new Exception("起点不需要前置条件");
        } else {
            //将第一个任务的状态设置为in_progress
            NodeStatus nodeStatus = new NodeStatus();
            nodeStatus.setNodeName((String)startingPoint.get("name"));
            nodeStatus.setNodeId((int)startingPoint.get("nodeId"));
            nodeStatus.setNodeStatus("in_progress");
            executionMapper.updateStatusOfNode(nodeStatus);
        }
        //将要执行的第一个任务返回出去
        return startingPoint;
    }



}

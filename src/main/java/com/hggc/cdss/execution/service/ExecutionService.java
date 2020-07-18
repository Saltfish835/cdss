package com.hggc.cdss.execution.service;

import com.hggc.cdss.Utils.ExecuteStringAsCodeUtils;
import com.hggc.cdss.execution.bean.DecisionResult;
import com.hggc.cdss.execution.bean.EnquiryResult;
import com.hggc.cdss.execution.bean.Graph;
import com.hggc.cdss.execution.bean.NodeStatus;
import com.hggc.cdss.execution.component.Loader;
import com.hggc.cdss.execution.mapper.ExecutionMapper;
import org.apache.commons.jexl2.JexlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    ExecuteStringAsCodeUtils executeStringAsCodeUtils;

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
        //判断个鬼，mysql表中所有的数据在刚开始全部都要删除
        executionMapper.deleteAllRecordOfDecisionResult();//删除决策结果表中全部数据
        graph =  loader.getGraph(diseaseName);//最开始为这个graph赋值
    }


    /**
     * 得到最开始的任务，也就是第一个任务
     * 这个方法只执行一次，故默认刚开始节点状态都为休眠
     * 所以不对节点的状态进行判断
     * @return
     */
    public Map<String,Object> getFristTask() throws Exception{
        Map<String,Object> errorMap = new HashMap<>();//保存错误信息的map
        //从graph中获取图的起点
        Map<String,Object> startingPoint = (Map<String,Object>)graph.getStaertingPoint();
        //判断起点的前置条件
        if(!((String)startingPoint.get("precondition")).equals("")) {
            errorMap.put("errorCode",500);//500表示后台有错误
            errorMap.put("errorMsg","起点不需要前置条件");//错误信息
            return errorMap;
            //throw new Exception("起点不需要前置条件");
        } else if(((String)startingPoint.get("task_type")).equals("decision")) {
            errorMap.put("errorCode",500);//500表示后台有错误
            errorMap.put("errorMsg","起点不能是decision任务");//错误信息
            return errorMap;
            //throw new Exception("起点不能是decision任务");
        } else {
            //将第一个任务的状态设置为in_progress
            NodeStatus nodeStatus = new NodeStatus();
            nodeStatus.setNodeName((String)startingPoint.get("name"));
            nodeStatus.setNodeId((int)startingPoint.get("nodeId"));
            nodeStatus.setNodeStatus("in_progress");
            executionMapper.updateStatusOfNode(nodeStatus);
        }
        //将要执行的第一个任务返回出去
        startingPoint.put("errorCode",200);//200代表正常
        return startingPoint;
    }


    /**
     * 将用户回答的问题答案存入数据库
     * @param resultArr
     */
    public void saveEnuiryResult(List<Map> resultArr) throws Exception{
        for(Map<String,Object> result : resultArr) {//遍历结果
            //将每个结果封装成对象
            EnquiryResult enquiryResult = new EnquiryResult();
            enquiryResult.setSourceName((String)result.get("source_name"));
            enquiryResult.setSourceCaption((String)result.get("source_caption"));
            enquiryResult.setResult((String)result.get("result"));
            enquiryResult.setSelectModel((String)result.get("select_model"));
            enquiryResult.setNodeId((int)result.get("node_id"));
            //数据类型也要保存一下
            //首先得到节点
            Map<String,Object> node = (Map<String,Object>)graph.getNodeByNodeIndex(graph.convertNodeIdToIndex((int)result.get("node_id")));
            //然后根据source_name得到数据类型
            String dataType = "";
            List<Map<String,Object>> enquirySources = (List<Map<String,Object>>)node.get("enquiry_source");
            for(Map<String,Object> enquirySource : enquirySources) {
                if(((String)enquirySource.get("name")).equals((String)result.get("source_name"))) {
                    dataType = (String)enquirySource.get("data_type");
                }
            }
            enquiryResult.setDataType(dataType);
            //将结果存入数据库
            executionMapper.addDataToEnquiryResult(enquiryResult);
        }
    }


    /**
     * 根据nodeId修改该节点在节点状态表中的状态
     * @param nodeId
     * @param status
     */
    public void updateStatusOfNodeByNodeId(int nodeId,String status) {
        NodeStatus nodeStatus = new NodeStatus();
        nodeStatus.setNodeId(nodeId);
        nodeStatus.setNodeStatus(status);
        executionMapper.updateStatusOfNode(nodeStatus);
    }


    /**
     * 得到下一个要执行的任务，规定一次只能执行一个任务
     * @param currentTaskId
     * @return
     */
    public Map<String,Object> getNextTasks(int currentTaskId) throws Exception{
        Map<String,Object> errorMap = new HashMap<>();//保存错误信息的map
        List<Map<String,Object>> needTORunTaskList = new ArrayList<>();
        //当前节点是已完成或者是丢弃状态才能继续进行下一个任务
        String status = executionMapper.getNodeStatusByNodeId(currentTaskId);
        if("dormant".equals(status) || "in_progress".equals(status)) {
            errorMap.put("errorCode",500);//500表示后台有错误
            errorMap.put("errorMsg","当前节点状态为休眠或正在进行，故不能开始下一个任务");//错误信息
            return errorMap;
            //throw new Exception("当前节点状态为休眠或正在进行，故不能开始下一个任务");
        }else {
            //获取这个节点后面的节点
            List<Map<String,Object>> nextTasks = graph.getAllSon(graph.convertNodeIdToIndex(currentTaskId));
            if(nextTasks.size() == 0) {//后面已经没有节点要执行了
                errorMap.put("errorCode",500);//500表示后台有错误
                errorMap.put("errorMsg","已经没有节点需要执行了");//错误信息
                return errorMap;
                //throw new Exception("已经没有节点需要执行了");
            }else {
                for(Map<String,Object> task :nextTasks) {
                    //这个循环里面应该只有一个task满足条件，因为一次只能执行一个任务
                    try {
                        if(this.checkPrecondidation(task) && "dormant".equals(executionMapper.getNodeStatusByNodeId((int)task.get("nodeId")))) {//check...可能会抛异常
                            //如果这个节点的前置条件满足，且这个节点当前状态为休眠，那么这个节点可以被执行
                            needTORunTaskList.add(task);//将这个节点添加到需要执行的节点的列表中
                            //将该节点的状态从休眠改成正在进行中
                            this.updateStatusOfNodeByNodeId((int)task.get("nodeId"),"in_progress");
                        }else {
                            //否则该节点就不满足执行条件，将该节点的状态设置为“discard”丢弃
                            this.updateStatusOfNodeByNodeId((int)task.get("nodeId"),"discard");
                        }
                    }catch (JexlException je) {//捕获用户写的条件的异常
                        errorMap.put("errorCode",500);//500表示后台有错误
                        errorMap.put("errorMsg",je.getMessage());//错误信息
                        return errorMap;
                    }
                }
            }
        }
        Map<String,Object> needTORunTask;
        if(needTORunTaskList.size() > 1) {
            //如果下一次有多个任务需要执行，这是不允许的
            errorMap.put("errorCode",500);//500表示后台有错误
            errorMap.put("errorMsg","不允许多个任务同时执行");//错误信息
            return errorMap;
            //throw new Exception("不允许多个任务同时执行");
        } else if(needTORunTaskList.size() == 0) {
            //当前节点后面存在节点，但是后面的节点不满足条件
            errorMap.put("errorCode",500);//500表示后台有错误
            errorMap.put("errorMsg","已经没有节点需要执行了");//错误信息
            return errorMap;
        }else{
            //如果下一次只有一个任务要执行，还需要判断这个任务的类型
            //因为action、enquiry、plan任务都是直接把任务的数据发送给前端就可以，而decision任务需要做出推荐决策
            if(needTORunTaskList.get(0).get("task_type").equals("decision")) {
                try{
                    needTORunTask = getRecommendation(needTORunTaskList.get(0));//得到这个decision任务的推荐结果
                }catch (JexlException je) {
                    errorMap.put("errorCode",500);//500表示后台有错误
                    errorMap.put("errorMsg",je.getMessage());//错误信息
                    return errorMap;
                }
            }else {
                needTORunTask = needTORunTaskList.get(0);
            }
        }
        needTORunTask.put("errorCode",200);//200代表正常
        return needTORunTask;//将下一步需要执行的任务返回给前端
    }


    /**
     * 判断该节点的前置条件是否满足
     * @param node
     * @return
     */
    public boolean checkPrecondidation(Map<String,Object> node) {
        boolean flag = false;
        String precondition = (String)node.get("precondition");
        if(precondition.equals("")) {//如果前置条件为空，则肯定满足
            flag = true;
        }else {//如果不为空，则需要进一步判断
            //先得到所有的问题的答案
            List<EnquiryResult> enquiryResultList = executionMapper.selectAllFromEnquiryResult();
            //封装成map集合,做参数用
            Map<String,Object> parameter = new HashMap<>();
            for(EnquiryResult enquiryResult:enquiryResultList) {
                parameter.put(enquiryResult.getSourceName(),enquiryResult.getResult());
            }
            //再得到所有的决策结果
            List<DecisionResult> decisionResultList = executionMapper.selectAddFromDecisionResult();
            //封装成map集合，当参数用
            for(DecisionResult decisionResult:decisionResultList) {
                parameter.put(decisionResult.getDecisionName(),decisionResult.getCandidateName());//这个decision的结果就是这个candidate
            }
            if((boolean)executeStringAsCodeUtils.executeString(precondition,parameter) == true) {//关键所在
                flag = true;//该任务前置条件成立
            }else {
                flag = false;//该任务前置条件不成立
            }

        }
        return flag;
    }


    /**
     * 如果史decision节点，则要得到推荐，然后给后台
     * @param decisionTask
     * @return
     */
    public Map<String,Object> getRecommendation(Map<String,Object> decisionTask) {
        Map<String,Object> newDecisionTask = new HashMap<>();
        newDecisionTask.put("task_type",decisionTask.get("task_type"));
        newDecisionTask.put("nodeId",decisionTask.get("nodeId"));
        newDecisionTask.put("name",decisionTask.get("name"));
        newDecisionTask.put("caption",decisionTask.get("caption"));
        newDecisionTask.put("description",decisionTask.get("description"));
        newDecisionTask.put("precondition",decisionTask.get("precondition"));
        //根据decision任务中填写的规则，得到推荐，然后将推荐结果封装回decisionTask中，返回前端显示
        List<Map<String,Object>> recommendationList = new ArrayList<>();
        //先得到所有的问题的答案
        List<EnquiryResult> enquiryResultList = executionMapper.selectAllFromEnquiryResult();
        //封装成map集合,做参数用
        Map<String,Object> parameter = new HashMap<>();
        for(EnquiryResult enquiryResult:enquiryResultList) {
            parameter.put(enquiryResult.getSourceName(),enquiryResult.getResult());
        }
        //得到这个decision任务的所有的candidate
        List<Map<String,Object>> candidateList = (List<Map<String,Object>>)decisionTask.get("candidates");
        //开始遍历每一个candidate
        for(Map<String,Object> candidate:candidateList) {
            Map<String,Object> recommendation = new HashMap<>();//封装recommendation的数据
            recommendation.put("name",candidate.get("name"));
            recommendation.put("caption",candidate.get("caption"));
            recommendation.put("priority",candidate.get("priority"));
            int weight = 0;//统计这个candidate的最终的支持度
            List<Map<String,Object>> recommendBasisList = new ArrayList<>();//记录推荐依据
            //得到这个candidate的所有argument
            List<Map<String,Object>> argumentList = (List<Map<String,Object>>)candidate.get("arguments");
            //开始遍历每一个argument
            for(Map<String,Object> argument:argumentList) {
                //得到这个argument的论据
                String condition = (String)argument.get("condition");
                System.out.println(condition);
                //判断这个论据是否为真
                if((boolean)executeStringAsCodeUtils.executeString(condition,parameter) == true) {//关键所在！！！
                    //说明这个论据是成立的，也就是说这个条论据对这个candidate有支持或者反对作用
                    //故开始统计权重
                    //int support = (int)argument.get("support");
                    int support = Integer.valueOf((String)argument.get("support"));
                    weight += support;
                    //这条论据是推荐依据，所以记录下来
                    recommendBasisList.add(argument);
                }
            }
            //把这个candidate的最终权重记录下来
            recommendation.put("weight",weight);
            //把支持或者反对这个candidate的论据记录下来
            recommendation.put("recommendBasisList",recommendBasisList);
            //把所有的recommendation记录下来
            recommendationList.add(recommendation);
        }
        //对recommendation做一个排序，按权重从大到小排，如果权重相等，则再比较priority
        //List<Map<String,Object>> recommendationListTemp = new ArrayList<>();
        //冒泡排序搞起
        for(int i=0;i<recommendationList.size();i++) {
            for(int j=0;j<recommendationList.size()-1;j++) {
                Map<String,Object> frontTemp = recommendationList.get(j);
                Map<String,Object> rearTemp = recommendationList.get(j+1);
                if((int)rearTemp.get("weight") > (int)frontTemp.get("weight")) {//如果后面的值大于前面的值，两者交换位置
                    recommendationList.set(j,rearTemp);//set方法会替换掉原来位置的值，而add是添加，后面元素下移
                    recommendationList.set(j+1,frontTemp);
                } else if((int)rearTemp.get("weight") == (int)frontTemp.get("weight")) {//如果两者的权重相等，则比较则再比较priority
                    if(Integer.valueOf((String)rearTemp.get("priority")) > Integer.valueOf((String)frontTemp.get("priority"))) {
                        recommendationList.set(j,rearTemp);//set方法会替换掉原来位置的值，而add是添加，后面元素下移
                        recommendationList.set(j+1,frontTemp);
                    }
                }
            }
        }
        newDecisionTask.put("recommendationList",recommendationList);
        return newDecisionTask;
    }




    public void saveDecisionResult(Map<String,Object> decisionResultMap) {
        //将结果封装成对象
        DecisionResult decisionResult = new DecisionResult();
        decisionResult.setNodeId((int)decisionResultMap.get("node_id"));
        decisionResult.setCandidateName((String)decisionResultMap.get("candidate_name"));
        decisionResult.setDecisionName((String)decisionResultMap.get("decision_name"));
        //存入数据库
        executionMapper.addDataToDecisionResult(decisionResult);
    }





}

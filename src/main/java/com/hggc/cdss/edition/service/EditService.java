package com.hggc.cdss.edition.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hggc.cdss.Utils.MongoDBUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EditService {

    @Autowired
    MongoDBUtils mongoDBUtils;

    /**
     * 通过疾病名称查询出任务网络
     * 返回值一个任务网络的map集合,但是包含_id属性，实际使用时去掉即可
     * @param disease
     * @return
     * @throws JsonProcessingException
     */
    public Map findTaskNetworkByDisease(String disease) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        //得到mongodb的连接
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        //获取集合，相当于指定表
        MongoCollection<Document> collection = mongoDatabase.getCollection("guidelines");
        //指定查询过滤器
        Bson filter = Filters.eq("disease_name", disease);
        //指定查询过滤器查询
        FindIterable findIterable = collection.find(filter).projection(new BasicDBObject("task_network",1));//只查出task_network这个字段,其实还有id这个字段
        MongoCursor cursor = findIterable.iterator();
        Map<String,Object> network = new HashMap<String,Object>();
        while (cursor.hasNext()) {
            network = objectMapper.readValue(objectMapper.writeValueAsString(cursor.next()), Map.class);//将查出的结果封装成map集合
        }
        return network;
    }


    /**
     * 根据疾病名称更新该疾病的任务网络
     * 如果有节点在task_table中,而不在传过来的任务网络中，说明该节点被用户删除，所以task_table中保存的数据也应该被删除
     * @param disease
     * @param newNetwork
     * @throws Exception
     */
    public void updateNetwork(String disease,String newNetwork) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,Object> networkMap = new HashMap<String,Object>();
        networkMap = objectMapper.readValue(newNetwork,Map.class);//将传过来的字符串变成map集合
        //得到mongodb的连接
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        //获取集合，相当于指定表
        MongoCollection<Document> collection = mongoDatabase.getCollection("guidelines");
        //更新任务网络
        collection.updateMany(Filters.eq("disease_name",disease),new Document("$set",new Document("task_network",networkMap)));
        //保持任务网络和task_table的一致
        this.maintainTaskTableAndNetworkConsistency(networkMap,disease);
    }


    /**
     * 如果有的节点在task_table中，而不再任务网络中，则在task_table中删除该节点，保持任务网络和task_table的一致性
     * 如果有的节点在任务网络中，而不再task_table中，则在task_table中添加该节点，保持任务网络和task_table的一致性
     * @param network
     * @param disease
     */
    private void maintainTaskTableAndNetworkConsistency(Map network,String disease) throws Exception{
        //先得到network中所有节点的id
        List<Map<String,Object>> nodeDataArray = (List<Map<String,Object>>)network.get("nodeDataArray");
        List keys = new ArrayList();
        for(Map node : nodeDataArray) {
            keys.add(node.get("key"));
        }
        System.out.println(keys);
        //得到task_table的所有节点
        ObjectMapper objectMapper = new ObjectMapper();
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        MongoCollection<Document> collection = mongoDatabase.getCollection("guidelines");
        //从数据库中查出task_table的数据
        FindIterable findIterable = collection.find(Filters.eq("disease_name",disease)).projection(new BasicDBObject("task_table",1));//这个结果还包含id
        MongoCursor cursor = findIterable.iterator();
        Map<String,Object> task_table = new HashMap<String,Object>();
        while (cursor.hasNext()) {
            task_table = objectMapper.readValue(objectMapper.writeValueAsString(cursor.next()), Map.class);//将查出的结果封装成map集合
        }
        List<Map<String,Object>> taskList = (List<Map<String,Object>>)task_table.get("task_table");//数据库返回的task_table中所有的数据
        //开始找出要删除的节点
        List<Integer> needDeleteNodes = new ArrayList();
        for(int i=0;i<taskList.size();i++) {//遍历task_table
            int nodeId = (int)taskList.get(i).get("nodeId");//拿出task_table中一个节点的id
            if(!keys.contains(nodeId)) {//如果任务网络中不包含这个节点，但是这个节点却又在task_table中，说明该节点要删除
                needDeleteNodes.add(i);//记录要删除的节点
            }
        }
        //将task_table里面需要删除的节点删除
        for(Integer i : needDeleteNodes) {
            taskList.remove((int)i);
        }

        //开始找出要添加的节点（在network中，而不在task_table中，最终需要把这种节点添加到task_table中）
        List taskTableKeys = new ArrayList();
        for(Map taskTableNode:taskList) {
            taskTableKeys.add(taskTableNode.get("nodeId"));
        }
        System.out.println(taskTableKeys);
        List<Integer> needAddNodes = new ArrayList<>();//保存需要添加的节点的id
        for(int i=0;i<keys.size();i++) {
            int networkNodeId = (int)keys.get(i);//拿出network中一个节点的id
            if(!taskTableKeys.contains(networkNodeId)) {//如果task_table中不包含这个节点，但是这个黑点却在任务网络中出现，说明该节点要添加到task_table中
                needAddNodes.add(networkNodeId);//记录要添加节点的id
            }
        }
        for(Map node : nodeDataArray) {//遍历任务网络
            if(needAddNodes.contains((Integer) node.get("key"))) {//如果这个node的id在needAddNodes中，说明这个node需要被添加到task_table中去
                Map<String,Object> addNode = new HashMap<>();//准备添加到task_table中去的节点
                if(((String)node.get("text")).equals("enq")) {//说明要向task_table中添加一个enquiry类型的节点
                    //封装数据
                    addNode.put("task_type","enquiry");
                    addNode.put("nodeId",(int)node.get("key"));
                    addNode.put("name","");
                    addNode.put("caption","");
                    addNode.put("description","");
                    addNode.put("precondition","");
                    addNode.put("enquiry_source",new ArrayList<>());
                    //将节点添加到task_table的一个”代表“中去
                    taskList.add(addNode);
                } else if(((String)node.get("text")).equals("dec")) {//说明要向task_table中添加一个decision类型的节点
                    //封装数据
                    addNode.put("task_type","decision");
                    addNode.put("nodeId",(int)node.get("key"));
                    addNode.put("name","");
                    addNode.put("caption","");
                    addNode.put("description","");
                    addNode.put("precondition","");
                    addNode.put("candidates",new ArrayList<>());
                    //将节点添加到task_table的一个”代表“中去
                    taskList.add(addNode);
                }else if(((String)node.get("text")).equals("act")) {//说明要向task_table中添加一个action类型的节点
                    //封装数据
                    addNode.put("task_type","action");
                    addNode.put("nodeId",(int)node.get("key"));
                    addNode.put("name","");
                    addNode.put("caption","");
                    addNode.put("description","");
                    addNode.put("precondition","");
                    addNode.put("content","");
                    //将节点添加到task_table的一个”代表“中去
                    taskList.add(addNode);
                }else if(((String)node.get("text")).equals("plan")) {//说明要向task_table中添加一个plan类型的节点
                    //封装数据
                    addNode.put("task_type","action");
                    addNode.put("nodeId",(int)node.get("key"));
                    addNode.put("name","");
                    addNode.put("caption","");
                    addNode.put("description","");
                    addNode.put("precondition","");
                    addNode.put("content","");
                    //将节点添加到task_table的一个”代表“中去
                    taskList.add(addNode);
                }
            }
        }

        //用taskList更新数据库,这样数据库中task_table就和task_network中节点保持一致了
        collection.updateMany(Filters.eq("disease_name",disease),new Document("$set",new Document("task_table",taskList)));


    }


    /**
     * 根据疾病名称找到该疾病的记录，然后从该疾病的记录中找到
     * 和传过来的nodeid相等的对应节点
     * @param disease
     * @param nodeId
     * @return
     * @throws Exception
     */
    public Map getNodeInfoByDiseaseAndId(String disease,int nodeId) throws Exception{
        List<Map<String,Object>> taskList = this.getAllTask(disease);
        Map<String,Object> node = new HashMap<>();
        for(Map<String,Object> map:taskList) {
            if((int)map.get("nodeId") == nodeId) {
                node = map;
            }
        }
        return node;
    }

    public void updateNodeInfo(String disease,int nodeId,String newNodeInfo) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        //得到mongodb的连接
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        //获取集合，相当于指定表
        MongoCollection<Document> collection = mongoDatabase.getCollection("guidelines");
        List<Map<String,Object>>taskList = this.getAllTask(disease);
        int sign = 99999;//用于记录用户点击的那个节点存在task_table的哪个位置
        for(int i=0;i<taskList.size();i++) {
            Map node = taskList.get(i);
            if((int)node.get("nodeId") == nodeId) {
                sign = i;
                break;//找到了就直接返回
            }
        }
        //先将新更新的过的节点信息字符串变成map集合
        Map<String,Object> newNodeInfoMap = objectMapper.readValue(newNodeInfo,Map.class);
        if(sign != 99999) {//如果找到了
            //删掉原来的信息
            taskList.remove(sign);
            //将新的添加进去
            taskList.add(sign,newNodeInfoMap);
        }else {//如果没找到，说明这是一个新节点，直接插入到task_table里面即可
            taskList.add(newNodeInfoMap);
        }
        //再更新到数据库里面去
        System.out.println("用于更新task_table的数据");
        System.out.println(taskList);
        UpdateResult result = collection.updateMany(Filters.eq("disease_name",disease),new Document("$set",new Document("task_table",taskList)));
        System.out.println("此次更新修改行数");
        System.out.println(result.getModifiedCount());
    }


    /**
     * 拿到所有decision和enquiry类型的任务
     * @param disease
     * @return
     * @throws Exception
     */
    public List<Map<String,Object>> getEnquiryAndDecision(String disease) throws Exception{
        List<Map<String,Object>> taskList = this.getAllTask(disease);
        List<Map<String,Object>> enquiryAndDecisionTaskList = new ArrayList<>();
        //从中拿出enquiry和decision类型的任务
        for(Map<String,Object> task:taskList) {
            if(((String)task.get("task_type")).equals("decision") || ((String)task.get("task_type")).equals("enquiry")) {
                enquiryAndDecisionTaskList.add(task);
            }
        }
        return enquiryAndDecisionTaskList;
    }


    /**
     * 得到所有任务
     * @param diseaseName
     * @return
     * @throws Exception
     */
    public List<Map<String,Object>> getAllTask(String diseaseName)throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        //得到mongodb的连接
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        //获取集合，相当于指定表
        MongoCollection<Document> collection = mongoDatabase.getCollection("guidelines");
        //先查出task_table的全部信息
        //查询条件
        BasicDBObject query = new BasicDBObject();
        query.put("disease_name",diseaseName);
        //指定查询过滤器查询
        FindIterable findIterable = collection.find(query).projection(new BasicDBObject("task_table",1));//只查出task_table这个字段
        MongoCursor cursor = findIterable.iterator();
        //先查出整个task_table
        Map<String,Object> task_table = new HashMap<String,Object>();
        while (cursor.hasNext()) {
            task_table = objectMapper.readValue(objectMapper.writeValueAsString(cursor.next()), Map.class);//将查出的结果封装成map集合
        }
        List<Map<String,Object>> taskList = (List<Map<String,Object>>)task_table.get("task_table");
        return taskList;
    }


}

package com.hggc.cdss.execution.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hggc.cdss.Utils.MongoDBUtils;
import com.hggc.cdss.execution.bean.Graph;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载整个临床指南到内存中
 *
 */
@Service
public class Loader {

    @Autowired
    MongoDBUtils mongoDBUtils;

    /**
     * 根据疾病名称获取疾病的指南
     * @param diseaseName
     * @return
     */
    public Map getGuideline(String diseaseName)throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        //得到mongodb的连接
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        //获取集合，相当于指定表
        MongoCollection<Document> collection = mongoDatabase.getCollection("guidelines");
        //指定查询过滤器
        Bson filter = Filters.eq("disease_name", diseaseName);
        //指定查询过滤器查询
        FindIterable findIterable = collection.find(filter);//只查出task_network这个字段
        MongoCursor cursor = findIterable.iterator();
        Map<String,Object> guideline = new HashMap<String,Object>();
        while (cursor.hasNext()) {
            guideline = objectMapper.readValue(objectMapper.writeValueAsString(cursor.next()), Map.class);//将查出的结果封装成map集合
        }
        return guideline;
    }


    /**
     * 将任务网络用邻接矩阵表示出来
     * @param diseaseName
     * @return
     * @throws Exception
     */
    public Graph getGraph(String diseaseName) throws Exception{
        Map guideline = getGuideline(diseaseName);
        //得到所有节点
        List<Map<String,Object>> nodeList = (List<Map<String,Object>>)guideline.get("task_table");
        //将任务网络用邻接矩阵表示出来，构造方法已经将节点传入graph对象中，还需要插入边
        Graph graph = new Graph(nodeList.size(),nodeList);
        //得到存节点之间关系的数据
        List<Map<String,Object>> linkDataArray = (List<Map<String,Object>>)((Map<String, Object>)guideline.get("task_network")).get("linkDataArray");
        //遍历各个节点之间的关系
        for(Map<String,Object> link:linkDataArray) {
            //将节点之间的关系保存到邻接矩阵中
            int x = this.getIndexByNodeId(nodeList,(int)link.get("from"));//找到nodeId对应的节点在nodeList中的下标
            int y = this.getIndexByNodeId(nodeList,(int)link.get("to"));//找到nodeId对应的节点在nodeList中的下标
            //将边保存到邻接矩阵中，有边的话，对应的值为1，无则为0
            graph.insertEdge(x,y);
        }
        return graph;
    }


    /**
     * nodeList存入有多个node
     * 然后通过nodeId找出其对应node对象在nodeList中的下标
     * @param nodeList
     * @param nodeId
     * @return
     */
    private int getIndexByNodeId(List<Map<String,Object>> nodeList,int nodeId) throws Exception{
        int index = -1;//返回-1则说明该nodeId对应的node对象不在nodeList中
        for(int i=0;i<nodeList.size();i++) {
            if((int)nodeList.get(i).get("nodeId") == nodeId) {
                index = i;
                break;
            }
        }
        if(index == -1) {
            throw new Exception("该nodeId没有对应的节点");
        }
        return index;
    }



}

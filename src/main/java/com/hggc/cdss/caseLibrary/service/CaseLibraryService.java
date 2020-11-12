package com.hggc.cdss.caseLibrary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hggc.cdss.Utils.MongoDBUtils;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CaseLibraryService {


    @Autowired
    MongoDBUtils mongoDBUtils;

    /**
     * 得到所有的病例
     * @return
     */
    public List<Map> getAllCase() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map> caseList = new ArrayList<Map>();
        //得到mongodb的连接
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        //获取集合，相当于指定表
        MongoCollection<Document> collection = mongoDatabase.getCollection("caselibrary");
        //查询所有文档
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while(mongoCursor.hasNext()){
            //System.out.println(mongoCursor.next());
            Map<String,Object> patientCase = objectMapper.readValue(objectMapper.writeValueAsString(mongoCursor.next()), Map.class);//将查出的结果封装成map集合
            caseList.add(patientCase);
        }
        return caseList;
    }


    /**
     * 通过case_id查询一条数据
     * @param caseId
     * @return
     * @throws Exception
     */
    public Map<String,Object> getCaseByCaseId(String caseId) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,Object> patientCase = new HashMap<>();
        //得到mongodb的连接
        MongoDatabase mongoDatabase = mongoDBUtils.getConnect();
        //获取集合，相当于指定表
        MongoCollection<Document> collection = mongoDatabase.getCollection("caselibrary");
        //指定查询过滤器
        Bson filter = Filters.eq("case_id", caseId);
        FindIterable<Document> findIterable = collection.find(filter);
        MongoCursor cursor = findIterable.iterator();
        while (cursor.hasNext()) {
            patientCase = objectMapper.readValue(objectMapper.writeValueAsString(cursor.next()), Map.class);//将查出的结果封装成map集合
        }
        return patientCase;
    }
}

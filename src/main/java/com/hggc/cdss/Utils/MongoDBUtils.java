package com.hggc.cdss.Utils;


import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//加载mongdb的配置文件
@PropertySource(value={"classpath:mongodb.properties"},encoding = "utf-8")
@Component
public class MongoDBUtils {

    @Value("${mongodbhost}")
    private String mongodbhost;

    @Value("${mongodbport}")
    private int mongodbport;

    @Value("${mongodbdatabasename}")
    private String databaseName;

    @Value("${userName}")
    private String userName;

    @Value("${password}")
    private String password;

    @Value("${isRouteConnect}")
    private int isRouteConnect;

    //得到mongodb的数据库连接
    public MongoDatabase getConnect() throws Exception{
        MongoDatabase mongoDatabase =null;

        if(isRouteConnect == 0) {//如果是0，代表连接的是本地数据库
            //连接到mongodb服务
            MongoClient mongoClient = new MongoClient(this.mongodbhost,this.mongodbport);
            //连接到数据库
            mongoDatabase = mongoClient.getDatabase(this.databaseName);
            //返回数据库连接

        } else if(isRouteConnect == 1) {
            List<ServerAddress> adds = new ArrayList<>();
            //ServerAddress()两个参数分别为 服务器地址 和 端口
            ServerAddress serverAddress = new ServerAddress(mongodbhost, mongodbport);
            adds.add(serverAddress);
            List<MongoCredential> credentials = new ArrayList<>();
            //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
            MongoCredential mongoCredential = MongoCredential.createScramSha1Credential("admin", "admin", password.toCharArray());
            credentials.add(mongoCredential);
            //通过连接认证获取MongoDB连接
            MongoClient mongoClient = new MongoClient(adds, credentials);
            //得到数据库
            mongoDatabase = mongoClient.getDatabase(databaseName);

        }
        if(mongoDatabase == null) {
            throw new Exception("数据库连接失败");
        }
        return mongoDatabase;
    }
}

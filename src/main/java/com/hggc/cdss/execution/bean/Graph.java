package com.hggc.cdss.execution.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Graph {

    private List<Map<String,Object>> nodeList;//存储节点的列表
    private int[][]  edges;//邻接矩阵
    private int numOfEdges;//边的条数

    /**
     * 构造方法，覆盖掉无参构造
     * 图的邻接矩阵表示必须传递节点个数过来
     * @param n
     */
    public Graph(int n,List<Map<String,Object>> nodeList) {
        edges = new int[n][n];
        this.nodeList = nodeList;
        numOfEdges = 0;
    }


    /**
     * 得到这个图的节点个数
     * @return
     */
    public int getNumberOfNode() {
        return nodeList.size();
    }


    /**
     * 得到这个图边的个数
     * @return
     */
    public int getNumOfEdges() {
        return numOfEdges;
    }

    @Override
    public String toString() {
        return "Graph{" +
                "nodeList=" + nodeList +
                ", edges=" + Arrays.toString(edges) +
                ", numOfEdges=" + numOfEdges +
                '}';
    }

    /**
     * 按下标返回节点的内容
     * 返回的是节点的内容
     * 节点在列表中的下标就是该节点在邻接矩阵中位置的表示
     * @param i
     * @return
     */
    public Object getNodeByNodeIndex(int i) {
        return nodeList.get(i);
    }


    /**
     * 通过传入两个节点的下标，得到这个两个节点的边
     * 如果为1，说明有边，如果为0，说明没边
     * @param x
     * @param y
     * @return
     */
    public int getEdgeByNodeIndex(int x,int y) {
        return edges[x][y];
    }



    /**
     *
     * 插入边
     * @param x
     * @param y
     */
    public void insertEdge(int x,int y) {
        edges[x][y] = 1;//x和y之间有边的话，在邻接矩阵中用1表示
        numOfEdges++;
    }


    /**
     * 删除边
     * @param x
     * @param y
     */
    public void deleteEdge(int x,int y) {
        edges[x][y] = 0;//x和y之间没有边的话，在邻接矩阵中用0表示
        numOfEdges--;
    }


    /**
     * 得到指定下标节点的所有全部直接后继
     * @param index
     * @return
     */
    public List<Map<String,Object>> getAllSon(int index) {
        List<Map<String,Object>> sonList = new ArrayList();
        for(int j=0;j<nodeList.size();j++) {//固定行，遍历列
            if(edges[index][j] == 1) {
                sonList.add(nodeList.get(j));
            }
        }
        return sonList;
    }


    /**
     * 得到指定下标节点的所有全部直接前驱
     * @param index
     * @return
     */
    public List<Map<String,Object>> getAllParent(int index) {
        List<Map<String,Object>> parentList = new ArrayList<>();
        for(int i=0;i<nodeList.size();i++) {//固定列，遍历行
            if(edges[i][index] == 1) {
                parentList.add(nodeList.get(i));
            }
        }
        return parentList;
    }



    /**
     * 得到这个图的起点，也就是入度为0的点
     * 在这个项目中，入度为0的点只能有一个
     * @return
     */
    public Object getStaertingPoint() throws Exception{
        int[] sumArr = new int[nodeList.size()];
        for(int i=0;i<nodeList.size();i++) {
            for(int j=0;j<nodeList.size();j++) {
                sumArr[j] += edges[i][j];
            }
        }
        int index = -1;//记录第几个位置为0
        for(int i=0;i<sumArr.length;i++) {
            if(sumArr[i] == 0) {
                index = i;
            }
        }
        if(index == -1) {
            throw new Exception("这个图没有入度为0的点");
        }
        return nodeList.get(index);
    }


    /**
     * 如果用户只知道节点的id，则可以通过这个方法获取到节点在列表中的位置
     * @param nodeId
     * @return
     * @throws Exception
     */
    public int convertNodeIdToIndex(int nodeId) throws Exception{
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

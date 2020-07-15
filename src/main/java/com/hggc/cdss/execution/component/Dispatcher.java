package com.hggc.cdss.execution.component;


import com.hggc.cdss.execution.bean.Graph;

/**
 * 调度整个任务的执行过程
 */
public class Dispatcher {

    //任务网络的邻接矩阵表示形式是调度的根本
    private Graph graph;


    public Dispatcher(Graph graph) {
        this.graph = graph;
    }



}

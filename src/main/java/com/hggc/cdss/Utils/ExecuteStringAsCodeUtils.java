package com.hggc.cdss.Utils;


import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 这个工具类是为了能够把字符串当作java代码来执行
 */
@Component
public class ExecuteStringAsCodeUtils {


    /**
     * 这个方法能够将字符串当作java代码来执行
     * @param express
     * @return
     */
    public Object executeString(String express, Map<String,Object> parameter) {
        JexlEngine jexlEngine = new JexlEngine();
        Expression expression = jexlEngine.createExpression(express);//将参数中的字符串传进来
        JexlContext jexlContext = new MapContext();
        for(String key:parameter.keySet()) {//遍历传过来的参数
            jexlContext.set(key,parameter.get(key));//将传进来的参数替换到表达式中去
        }
        if(null == expression.evaluate(jexlContext)) {//执行表达式
            return "";//为空就返回空字符串
        }
        return expression.evaluate(jexlContext);//执行表达式，返回结果
    }


}

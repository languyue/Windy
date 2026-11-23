package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Data
public class QueryResponseModel {

  private Integer status;

  private Map<String, Object> context;

  private List<String> message = new ArrayList<>();

  /**
   * 节点返回的附加数据，供刷新上下文的断言比较使用，结构由各节点自行决定
   * */
  private Map<String, Object> data;

  public void addMessage(String msg) {
    message.add(msg);
  }

  public void addAllMessage(List<String> msgList) {
    message.addAll(msgList);
  }
}

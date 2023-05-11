package com.zj.client.entity;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/3/29
 */
@Data
public class ResponseModel {
  private Object data;
  private String message;

  public ResponseModel() {
  }

  public ResponseModel(Object data, String message) {
    this.data = data;
    this.message = message;
  }
}

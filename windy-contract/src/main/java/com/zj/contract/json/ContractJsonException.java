package com.zj.contract.json;

/**
 * 契约层 JSON 转换异常，属于编程/数据错误，不做静默降级。
 *
 * @author guyuelan
 */
public class ContractJsonException extends RuntimeException {

  public ContractJsonException(String message, Throwable cause) {
    super(message, cause);
  }
}

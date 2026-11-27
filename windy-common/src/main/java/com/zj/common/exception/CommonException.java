package com.zj.common.exception;
import com.zj.contract.exception.ErrorCode;


import lombok.Getter;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Getter
public class CommonException extends RuntimeException{

  private String code;
  private String msg;
  private int httpStatus;

  public CommonException() {
  }

  public CommonException(String code, String msg, int httpStatus) {
    super(msg);
    this.code = code;
    this.msg = msg;
    this.httpStatus = httpStatus;
  }
  public CommonException(ErrorCode errorCode, String content) {
    super(String.format(errorCode.getMessage(), content));
    this.code = errorCode.getCode();
    this.msg = String.format(errorCode.getMessage(), content);
    this.httpStatus = errorCode.getHttpStatus();
  }
}

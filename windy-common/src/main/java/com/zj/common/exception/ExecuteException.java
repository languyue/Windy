package com.zj.common.exception;
import com.zj.contract.exception.ErrorCode;


import java.util.Objects;
import org.springframework.http.HttpStatus;

/**
 * @author guyuelan
 * @since 2023/6/21
 */
public class ExecuteException extends CommonException {

  private String message;

  public ExecuteException(ErrorCode errorCode) {
    super(errorCode.getCode(), errorCode.getMessage(), HttpStatus.valueOf(errorCode.getHttpStatus()));
    this.message = errorCode.getMessage();

  }

  public ExecuteException(String message) {
    super();
    this.message = "execute error: " + message;
  }

  @Override
  public String getMessage() {
    if (Objects.isNull(getMsg())) {
      return message;
    }

    return getMsg();
  }
}

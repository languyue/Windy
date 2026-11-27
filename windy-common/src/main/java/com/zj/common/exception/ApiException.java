package com.zj.common.exception;
import com.zj.contract.exception.ErrorCode;


/**
 * @author guyuelan
 * @since 2021/9/28
 */
public class ApiException extends CommonException {

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }
}

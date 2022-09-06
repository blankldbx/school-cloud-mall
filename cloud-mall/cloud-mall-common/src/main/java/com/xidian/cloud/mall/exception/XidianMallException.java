package com.xidian.cloud.mall.exception;

/**
 * @author LDBX
 * 统一异常
 */
public class XidianMallException extends RuntimeException{
    private final Integer code;
    private final String message;

    public XidianMallException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public XidianMallException(XidianMallExceptionEnum exceptionEnum) {
        this(exceptionEnum.getCode(), exceptionEnum.getMsg());
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

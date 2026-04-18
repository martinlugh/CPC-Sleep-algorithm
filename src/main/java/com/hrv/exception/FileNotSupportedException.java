package com.hrv.exception;

/** 不支持的文件格式异常。 */
public class FileNotSupportedException extends RuntimeException {
    public FileNotSupportedException(String message) {
        super(message);
    }
}

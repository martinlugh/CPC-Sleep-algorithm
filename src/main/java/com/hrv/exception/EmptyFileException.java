package com.hrv.exception;

/** 空文件异常。 */
public class EmptyFileException extends RuntimeException {
    public EmptyFileException(String message) {
        super(message);
    }
}

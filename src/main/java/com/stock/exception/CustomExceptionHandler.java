package com.stock.exception;

import com.stock.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleServerException() {
        System.out.println("error from GlobalExceptionHandler");
        return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getStatus(), ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public ErrorResponse handleClientException() {
        System.out.println("invalid request");
        return new ErrorResponse(ErrorCode.INVALID_REQUEST.getStatus(), ErrorCode.INVALID_REQUEST.getDescription());
    }

    @ExceptionHandler(DividendException.class)
    protected ErrorResponse handleCustomException(DividendException e) {
        log.error("{} is occurred.", e.getErrorCode());
        return new ErrorResponse(e.getErrorStatus(), e.getErrorMessage());
    }
}

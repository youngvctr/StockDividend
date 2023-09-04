package com.stock.exception;

import com.stock.type.ErrorCode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DividendException extends RuntimeException {
    private ErrorCode errorCode;
    private int errorStatus;
    private String errorMessage;

    public DividendException(ErrorCode errorCode) {
        this.errorStatus = errorCode.getStatus();
        this.errorMessage = errorCode.getDescription();
    }
}

package com.stock.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(500, "내부 서버에서 오류가 발생했습니다."),
    USER_ALREADY_EXIST(400, "이미 존재하는 사용자입니다."),
    COMPANY_ALREADY_EXIST(400, "이미 존재하는 회사입니다."),
    INVALID_COMPANY(400, "회사명이 존재하지 않습니다."),
    INVALID_USER(400, "사용자명이 존재하지 않습니다."),
    UNMATCHED_USER_ID_PW(400, "아이디와 비밀번호가 맞지 않습니다."),
    FAIL_SCRAP_TICKER(400, "Ticker scrap 에 실패했습니다."),
    TICKER_IS_EMPTY(400, "Ticker 가 비어있습니다"),
    INVALID_REGISTER_REQUEST(400, "비 로그인 요청입니다."),
    INVALID_REQUEST(400, "잘못된 요청입니다.");

    private final String description;
    private final int status;

    ErrorCode(int status, String description){
        this.status = status;
        this.description = description;
    }
}

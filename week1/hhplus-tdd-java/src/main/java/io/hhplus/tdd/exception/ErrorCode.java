package io.hhplus.tdd.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_USER_ID("400", "유효하지 않은 사용자 ID입니다."),
    USER_NOT_FOUND("404", "존재하지 않는 사용자입니다."),
    INSUFFICIENT_BALANCE("400", "잔액이 부족합니다."),
    MAX_POINT_LIMIT_EXCEEDED("400", "포인트 최대 한도를 초과했습니다."),
    INVALID_AMOUNT("400", "유효하지 않은 금액입니다."),
    NULL_USER_ID("400", "사용자 ID는 null일 수 없습니다."),
    NEGATIVE_BALANCE("400", "잔액이 0 미만일 수 없습니다."),
    GENERAL_ERROR("500", "서버에서 에러가 발생했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}

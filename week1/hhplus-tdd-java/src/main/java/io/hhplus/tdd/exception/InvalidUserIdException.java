package io.hhplus.tdd.exception;

// 유효하지 않은 사용자 ID 예외 클래스.
public class InvalidUserIdException extends RuntimeException {

    // 기본 생성자
    public InvalidUserIdException() {
        super(ErrorCode.INVALID_USER_ID.getMessage());
    }

    // 메시지를 받는 생성자
    public InvalidUserIdException(String message) {
        super(message);
    }
}
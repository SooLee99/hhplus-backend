package io.hhplus.tdd.exception;

// 사용자 ID가 존재하지 않을 때 발생하는 예외
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
package io.hhplus.tdd.exception;

// 사용자 ID가 존재하지 않을 때 발생하는 예외
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("존재하지 않는 사용자 ID입니다.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
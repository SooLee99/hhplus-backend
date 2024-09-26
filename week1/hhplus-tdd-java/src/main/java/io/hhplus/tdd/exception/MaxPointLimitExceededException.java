package io.hhplus.tdd.exception;

    // 최대 포인트 한도를 초과할 때 발생하는 예외
public class MaxPointLimitExceededException extends RuntimeException {
    public MaxPointLimitExceededException() {
        super("포인트 최대 한도를 초과하였습니다.");
    }

    public MaxPointLimitExceededException(String message) {
        super(message);
    }
}
package io.hhplus.tdd.exception;

    // 최대 포인트 한도를 초과할 때 발생하는 예외
public class MaxPointLimitExceededException extends RuntimeException {
    public MaxPointLimitExceededException() {
        super(ErrorCode.MAX_POINT_LIMIT_EXCEEDED.getMessage());
    }

    public MaxPointLimitExceededException(String message) {
        super(message);
    }
}
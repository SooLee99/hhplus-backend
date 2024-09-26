package io.hhplus.tdd.exception;

// 포인트 잔액이 부족할 때 발생하는 예외
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
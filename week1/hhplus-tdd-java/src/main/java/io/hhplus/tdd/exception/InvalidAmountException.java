package io.hhplus.tdd.exception;

// 유효하지 않은 금액(0 이하)이 입력될 때 발생하는 예외
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException() {
        super("유효하지 않은 금액입니다.");
    }

    public InvalidAmountException(String message) {
        super(message);
    }
}

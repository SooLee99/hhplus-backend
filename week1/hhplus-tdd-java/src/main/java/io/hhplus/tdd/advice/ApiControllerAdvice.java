package io.hhplus.tdd.advice;

import io.hhplus.tdd.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiControllerAdvice {

    // 공통적으로 사용되는 ErrorResponse 생성 메서드
    private ResponseEntity<ErrorResponse> buildErrorResponse(String code, String message, HttpStatus status) {
        ErrorResponse errorResponse = new ErrorResponse(code, message);
        return new ResponseEntity<>(errorResponse, status);
    }

    // 존재하지 않는 사용자 예외 처리
    @ExceptionHandler({UserNotFoundException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(Exception ex) {
        return buildErrorResponse("400", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND);
    }

    // 포인트 최대 한도 초과 예외 처리
    @ExceptionHandler(MaxPointLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxPointLimitExceededException(MaxPointLimitExceededException ex) {
        return buildErrorResponse("400", "포인트 최대 한도를 초과했습니다.", HttpStatus.BAD_REQUEST);
    }

    // 유효하지 않은 금액 예외 처리
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmountException(InvalidAmountException ex) {
        return buildErrorResponse("400", "유효하지 않은 금액입니다.", HttpStatus.BAD_REQUEST);
    }

    // 잔액 부족 예외 처리
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        return buildErrorResponse("400", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST);
    }

    // 기타 일반적인 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return buildErrorResponse("500", "서버에서 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

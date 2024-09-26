package io.hhplus.tdd.advice;

import io.hhplus.tdd.exception.*;
import io.hhplus.tdd.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiControllerAdvice {

    // 공통적으로 사용되는 ErrorResponse 생성 메서드
    private ResponseEntity<ErrorResponse> buildErrorResponse(ErrorCode errorCode, HttpStatus status) {
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 사용자 미존재 예외 처리
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        log.error("사용자 미존재 예외 발생: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    /**
     * 포인트 최대 한도 초과 예외 처리
     */
    @ExceptionHandler(MaxPointLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxPointLimitExceededException(MaxPointLimitExceededException ex) {
        log.error("포인트 최대 한도 초과 예외 발생: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.MAX_POINT_LIMIT_EXCEEDED, HttpStatus.BAD_REQUEST);
    }

    /**
     * 유효하지 않은 금액 예외 처리
     */
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmountException(InvalidAmountException ex) {
        log.error("유효하지 않은 금액 예외 발생: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.INVALID_AMOUNT, HttpStatus.BAD_REQUEST);
    }

    /**
     * 잔액 부족 예외 처리
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        log.error("잔액 부족 예외 발생: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.INSUFFICIENT_BALANCE, HttpStatus.BAD_REQUEST);
    }

    /**
     * 유효하지 않은 사용자 ID 예외 처리
     */
    @ExceptionHandler(InvalidUserIdException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserIdException(InvalidUserIdException ex) {
        log.error("유효하지 않은 사용자 ID 예외 발생: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.INVALID_USER_ID, HttpStatus.BAD_REQUEST);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("서버 에러 발생: {}", ex.getMessage(), ex);
        return buildErrorResponse(ErrorCode.GENERAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

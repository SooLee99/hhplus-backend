package org.example.lecture.application.exception;

/**
 * [Application 도메인 예외]
 * - 신청 내역이 존재하지 않을 때 발생하는 예외.
 */
public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException() {
        super("해당 사용자의 강의 신청 내역을 찾을 수 없습니다.");
    }

    public ApplicationNotFoundException(String message) {
        super(message);
    }
}


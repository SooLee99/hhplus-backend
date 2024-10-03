package org.example.lecture.application.exception;

/**
 * [Application 도메인 예외]
 * - 동일한 강의에 대해 사용자가 중복 신청을 시도했을 때 발생하는 예외.
 */
public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException(Long userId, Long lectureId) {
        super("사용자 ID: " + userId + "가 이미 강의 ID: " + lectureId + "에 신청했습니다.");
    }
}

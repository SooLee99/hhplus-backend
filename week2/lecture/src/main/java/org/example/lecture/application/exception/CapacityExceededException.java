package org.example.lecture.application.exception;

/**
 * [Lecture 도메인 예외]
 * - 강의 슬롯이 정원 초과된 상태일 때 발생하는 예외.
 */
public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException(String message) {
        super(message);
    }

    public CapacityExceededException(Long lectureId, int maxCapacity) {
        super("해당 강의 슬롯이 정원 초과 상태입니다. Lecture ID: " + lectureId + ", 최대 정원: " + maxCapacity);
    }
}

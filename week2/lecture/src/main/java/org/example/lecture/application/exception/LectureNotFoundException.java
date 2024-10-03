package org.example.lecture.application.exception;

/**
 * [Lecture 도메인 예외]
 * - 특정 강의가 존재하지 않는 경우 발생하는 예외.
 */
public class LectureNotFoundException extends RuntimeException {
    public LectureNotFoundException() {
        super("해당 강의가 존재하지 않습니다.");
    }

    public LectureNotFoundException(String message) {
        super(message);
    }
}

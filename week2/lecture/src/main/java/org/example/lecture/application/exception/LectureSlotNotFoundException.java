package org.example.lecture.application.exception;

/**
 * [LectureSlotStatus 도메인 예외]
 * - 특정 슬롯 상태가 존재하지 않을 때 발생하는 예외.
 */
public class LectureSlotNotFoundException extends RuntimeException {
    public LectureSlotNotFoundException(Long slotId) {
        super("해당 슬롯의 상태 정보가 존재하지 않습니다. Slot ID: " + slotId);
    }
}

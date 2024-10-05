package org.example.lecture.application.exception;

public class LectureSlotStatusNotFoundException extends RuntimeException {
    public LectureSlotStatusNotFoundException(Long slotId) {
        super("해당 슬롯의 상태 정보가 존재하지 않습니다. Slot ID: " + slotId);
    }
}

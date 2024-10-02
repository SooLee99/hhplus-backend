package org.example.lecture.domain.status;

public enum LectureSlotStatusType {
    OPEN("신청 가능"),        // 강의 슬롯이 열려 있음
    FULL("정원 초과"),        // 강의 정원이 초과된 상태
    CLOSED("마감됨");         // 강의 신청이 마감된 상태

    private final String description;

    LectureSlotStatusType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // 특정 상태일 때만 신청 가능 여부 확인
    public boolean isAvailable() {
        return this == OPEN;
    }

    // 상태 변경 검증: 신청이 가능한 상태로 전환
    public static LectureSlotStatusType transitionToOpen() {
        return OPEN;
    }

    // 상태 변경 검증: 정원 초과로 변경
    public static LectureSlotStatusType transitionToFull() {
        return FULL;
    }

    // 상태 변경 검증: 신청 마감 상태로 변경
    public static LectureSlotStatusType transitionToClosed() {
        return CLOSED;
    }
}

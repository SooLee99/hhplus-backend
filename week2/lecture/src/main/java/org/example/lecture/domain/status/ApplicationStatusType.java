package org.example.lecture.domain.status;

public enum ApplicationStatusType {
    APPLIED("신청 완료"),          // 사용자가 신청한 상태
    CANCELED("신청 취소"),         // 사용자가 신청을 취소한 상태
    WAITING("대기 중"),            // 정원이 초과되어 대기자 명단에 있는 상태
    COMPLETED("신청 확정");        // 대기자에서 확정된 상태

    private final String description;

    ApplicationStatusType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // 상태 값으로부터 특정 비즈니스 로직 처리 가능
    public boolean isCancelable() {
        return this == APPLIED || this == WAITING;
    }

    // 상태 변경 검증: 신청 취소 상태로 변경
    public static ApplicationStatusType cancel() {
        return CANCELED;
    }

    // 상태 변경 검증: 대기 상태로 전환
    public static ApplicationStatusType waitForSlot() {
        return WAITING;
    }

    // 상태 변경 검증: 신청 확정 상태로 전환
    public static ApplicationStatusType completeApplication() {
        return COMPLETED;
    }
}

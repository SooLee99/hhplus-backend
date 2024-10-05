package org.example.lecture.domain.application;

import lombok.Getter;

@Getter
public enum ApplicationStatusType {
    APPLIED("신청 완료"),          // 사용자가 신청한 상태
    WAITING("대기 중");           // 정원이 초과되어 대기자 명단에 있는 상태

    private final String description;

    ApplicationStatusType(String description) {
        this.description = description;
    }

    // 상태 변경 검증: 대기 상태로 전환
    public static ApplicationStatusType waitForSlot() {
        return WAITING;
    }

    // 상태 변경 검증: 신청 상태로 전환
    public static ApplicationStatusType apply() {
        return APPLIED;
    }
}

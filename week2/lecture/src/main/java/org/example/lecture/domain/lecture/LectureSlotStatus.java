package org.example.lecture.domain.lecture;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * [강의 슬롯 상태 엔티티]
 * - 각 슬롯의 신청자 수, 대기자 수, 취소자 수를 관리하는 엔티티
 */
@Entity
@Table(name = "lecture_slot_status")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureSlotStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statusId;

    @OneToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private LectureSlot lectureSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LectureSlotStatusType status;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int currentApplicants;  // 현재 신청자 수

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int waitingList;  // 현재 대기자 수

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    public LectureSlotStatus(LectureSlot lectureSlot, LectureSlotStatusType status, int currentApplicants, int waitingList, int canceledApplicants) {
        this.lectureSlot = lectureSlot;
        this.status = status;
        this.currentApplicants = currentApplicants;
        this.waitingList = waitingList;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    // 신청자 수 증가 메서드
    public void incrementApplicants() {
        if (this.status.isAvailable()) {
            this.currentApplicants++;
            if (this.currentApplicants >= lectureSlot.getCapacity()) {
                this.status = LectureSlotStatusType.FULL;  // 정원 초과로 상태 변경
            }
            this.lastUpdatedAt = LocalDateTime.now();
        }
    }

    // 대기자 수 증가 메서드
    public void incrementWaitingList() {
        this.waitingList++;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // 상태 변경: 마감 처리
    public void closeRegistration() {
        this.status = LectureSlotStatusType.transitionToClosed();
        this.lastUpdatedAt = LocalDateTime.now();
    }


}

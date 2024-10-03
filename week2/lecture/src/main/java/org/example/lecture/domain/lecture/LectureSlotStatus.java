package org.example.lecture.domain.lecture;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * [강의 슬롯 상태 엔티티]
 * - 각 슬롯의 신청자 수, 대기자 수, 취소자 수를 관리하는 엔티티
 */
@Slf4j
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

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    public LectureSlotStatus(LectureSlot lectureSlot, LectureSlotStatusType status, int currentApplicants, int waitingList, int canceledApplicants) {
        this.lectureSlot = lectureSlot;
        this.status = status;
        this.currentApplicants = currentApplicants;
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

    public void changeStatus(LectureSlotStatusType newStatus) {
        this.status = newStatus;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}

package org.example.lecture.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.lecture.domain.status.LectureSlotStatusType;

import java.time.LocalDateTime;

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
    private int currentApplicants;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int waitingList;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int canceledApplicants;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    // 상태 변경 메서드
    public void incrementApplicants() {
        if (this.status.isAvailable()) {
            this.currentApplicants++;
            if (this.currentApplicants >= lectureSlot.getCapacity()) {
                this.status = LectureSlotStatusType.transitionToFull();
            }
            this.lastUpdatedAt = LocalDateTime.now();
        }
    }

    // 상태 변경: 마감 처리
    public void closeRegistration() {
        this.status = LectureSlotStatusType.transitionToClosed();
        this.lastUpdatedAt = LocalDateTime.now();
    }
}

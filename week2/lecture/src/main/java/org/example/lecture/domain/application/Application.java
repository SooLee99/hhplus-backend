package org.example.lecture.domain.application;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.example.lecture.domain.lecture.LectureSlot;

/**
 * 강의 신청 엔티티
 */
@Entity
@Table(name = "application")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id", length = 36, nullable = false)
    private Long applicationId;

    @ManyToOne
    @JoinColumn(name = "slot_id", nullable = false)
    private LectureSlot lectureSlot;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private ApplicationStatusType currentStatus;  // 신청 상태

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 상태 변경 메서드
    public void cancelApplication() {
        if (this.currentStatus.isCancelable()) {
            this.currentStatus = ApplicationStatusType.cancel();
        }
    }

    // 대기 상태로 변경
    public void moveToWaitingList() {
        this.currentStatus = ApplicationStatusType.waitForSlot();
    }
}
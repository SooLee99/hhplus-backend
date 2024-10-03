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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id", nullable = false)
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

    public Application(Long userId, LectureSlot slot) {
        this.userId = userId;
        this.lectureSlot = slot;
        this.currentStatus = ApplicationStatusType.waitForSlot();
        this.createdAt = LocalDateTime.now();
    }

    // 상태 변경 검증: 신청 상태로 전환
    public void apply() {
        this.currentStatus = ApplicationStatusType.apply();
    }

}
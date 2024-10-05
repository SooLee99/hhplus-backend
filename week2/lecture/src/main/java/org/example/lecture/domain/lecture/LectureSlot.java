package org.example.lecture.domain.lecture;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [특정 날짜의 강의 정원 정보 엔티티]
 * - 특정 날짜에 진행되는 강의의 정원 정보를 관리하는 엔티티
 */
@Entity
@Table(name = "lecture_slot")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class LectureSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id", length = 36, nullable = false)
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "date", nullable = false)
    private LocalDate date; // 강의가 진행되는 날짜

    @Column(name = "capacity", nullable = false, columnDefinition = "INT DEFAULT 30")
    private int capacity;   // 수용 가능 인원

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

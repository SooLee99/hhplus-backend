package org.example.lecture.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [강의 날짜별 정원 엔티티]
 */
@Entity
@Table(name = "lecture_slot")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureSlot {

    @Id
    @Column(name = "slot_id", length = 36, nullable = false)
    private String slotId;

    @ManyToOne
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "capacity", nullable = false, columnDefinition = "INT DEFAULT 30")
    private int capacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 상태 확인 메서드
    public boolean isFull() {
        return this.capacity <= 0;
    }

    // 정적 팩토리 메서드
    public static LectureSlot create(Lecture lecture, LocalDate date, int capacity) {
        return LectureSlot.builder()
                .lecture(lecture)
                .date(date)
                .capacity(capacity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}

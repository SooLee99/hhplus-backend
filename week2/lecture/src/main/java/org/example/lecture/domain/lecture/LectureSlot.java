package org.example.lecture.domain.lecture;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * [특정 날짜의 강의 정원 정보 엔티티]
 * - 특정 날짜의 강의 슬롯 관리하는 엔티티 클래스
 */
@Entity
@Table(name = "lecture_slot")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id", length = 36, nullable = false)
    private Long slotId;

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

    // 슬롯이 정원을 초과했는지 확인하는 메서드
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

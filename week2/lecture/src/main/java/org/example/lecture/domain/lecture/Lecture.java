package org.example.lecture.domain.lecture;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * [강의 정보 엔티티]
 * - 강의 정보를 관리하는 읽기 전용 엔티티 클래스 (불변 객체 - 수정, 삭제 불가)
 */

@Entity
@Table(name = "lecture")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long lectureId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "instructor", nullable = false, length = 255)
    private String instructor;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_capacity", nullable = false, columnDefinition = "INT DEFAULT 30")
    private int maxCapacity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
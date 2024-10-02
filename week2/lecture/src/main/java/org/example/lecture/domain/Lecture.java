package org.example.lecture.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "lecture")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lecture {

    @Id
    @Column(name = "lecture_id", length = 36, nullable = false)
    private String lectureId;

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

    public static Lecture create(String lectureId, String name, String instructor, String description, int maxCapacity) {
        return Lecture.builder()
                .lectureId(lectureId)
                .name(name)
                .instructor(instructor)
                .description(description)
                .maxCapacity(maxCapacity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
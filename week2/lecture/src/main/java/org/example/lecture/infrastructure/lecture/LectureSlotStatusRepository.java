package org.example.lecture.infrastructure.lecture;

import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * LectureSlotStatus 엔티티를 관리하는 레파지토리
 */
@Repository
public interface LectureSlotStatusRepository extends JpaRepository<LectureSlotStatus, Long> {

    Optional<LectureSlotStatus> findByLectureSlot(LectureSlot lectureSlot);
}
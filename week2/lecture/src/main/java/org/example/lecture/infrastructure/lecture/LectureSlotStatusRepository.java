package org.example.lecture.infrastructure.lecture;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * LectureSlotStatus 엔티티를 관리하는 레파지토리
 */
@Repository
public interface LectureSlotStatusRepository extends JpaRepository<LectureSlotStatus, Long> {

    Optional<LectureSlotStatus> findByLectureSlot(LectureSlot lectureSlot);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "javax.persistence.lock.timeout", value = "3000") // 타임아웃 설정 (밀리초)
    })
    @Query("SELECT l FROM LectureSlotStatus l WHERE l.statusId = :slotId")
    Optional<LectureSlotStatus> findBySlotIdWithPessimisticLock(@Param("slotId") Long slotId);
}
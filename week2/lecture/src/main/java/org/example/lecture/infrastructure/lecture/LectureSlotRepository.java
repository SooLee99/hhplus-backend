package org.example.lecture.infrastructure.lecture;

import jakarta.persistence.LockModeType;
import org.example.lecture.domain.lecture.LectureSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * LectureSlot 엔티티를 관리하는 JPA 레파지토리
 */
public interface LectureSlotRepository extends JpaRepository<LectureSlot, Long> {

    // 특정 날짜에 해당하는 강의 슬롯 목록 조회
    List<LectureSlot> findByDate(LocalDate date);

    /**
     * Pessimistic Lock을 이용하여 강의 슬롯을 조회
     * @param slotId 조회할 강의 슬롯 ID
     * @return 강의 슬롯 (락이 적용된 상태)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ls FROM LectureSlot ls WHERE ls.slotId = :slotId")
    Optional<LectureSlot> findBySlotIdWithPessimisticLock(Long slotId);

}


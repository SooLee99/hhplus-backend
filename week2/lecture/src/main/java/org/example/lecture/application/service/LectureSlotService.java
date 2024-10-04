package org.example.lecture.application.service;

import org.example.lecture.application.exception.LectureSlotNotFoundException;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
import org.example.lecture.infrastructure.lecture.LectureSlotRepository;
import org.example.lecture.infrastructure.lecture.LectureSlotStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LectureSlotService {

    private final LectureSlotRepository lectureSlotRepository;
    private final LectureSlotStatusRepository lectureSlotStatusRepository;

    public LectureSlotService(LectureSlotRepository lectureSlotRepository,
                              LectureSlotStatusRepository lectureSlotStatusRepository) {
        this.lectureSlotRepository = lectureSlotRepository;
        this.lectureSlotStatusRepository = lectureSlotStatusRepository;
    }

    /**
     * [2. 특강 선택 API - 특정 날짜의 강의 슬롯 목록 조회]
     * - 특정 날짜에 해당하는 강의 슬롯 목록을 반환.
     */
    public List<LectureSlot> getLectureSlotsByDate(LocalDate date) {
        return lectureSlotRepository.findByDate(date);
    }

    /**
     * [특정 강의 슬롯에 대한 선착순 정원 예약 - 비관적 락 적용]
     * - 데이터베이스의 PESSIMISTIC_WRITE 락을 사용하여 동시성 제어를 구현.
     *
     * @param lectureSlotId 예약할 강의 슬롯 ID
     * @return 예약된 강의 슬롯 엔티티
     */
    @Transactional
    public LectureSlot reserveSlotWithLock(Long lectureSlotId) {
        // 비관적 락을 사용하여 강의 슬롯 상태를 조회
        LectureSlotStatus slotStatus = lectureSlotStatusRepository.findBySlotIdWithPessimisticLock(lectureSlotId)
                .orElseThrow(() -> new LectureSlotNotFoundException(lectureSlotId));

        // 강의 슬롯 정보 조회
        LectureSlot lectureSlot = lectureSlotRepository.findById(lectureSlotId)
                .orElseThrow(() -> new LectureSlotNotFoundException(lectureSlotId));

        // 강의 슬롯 상태 검증
        if (slotStatus.getStatus() == LectureSlotStatusType.FULL) {
            throw new IllegalStateException("강의 정원이 초과되었습니다.");
        } else if (slotStatus.getStatus() == LectureSlotStatusType.CLOSED) {
            throw new IllegalStateException("강의 신청이 마감되었습니다.");
        }

        // 신청자 수 증가
        slotStatus.incrementApplicants();

        // 상태 변경: 정원이 꽉 찼으면 FULL, 남아있으면 OPEN으로 상태를 유지
        if (slotStatus.getCurrentApplicants() >= lectureSlot.getCapacity()) {
            slotStatus.changeStatus(LectureSlotStatusType.FULL);
        } else {
            slotStatus.changeStatus(LectureSlotStatusType.OPEN);
        }

        // 변경된 상태 저장
        lectureSlotStatusRepository.save(slotStatus);

        // 강의 슬롯 정보 반환
        return lectureSlot;
    }
}

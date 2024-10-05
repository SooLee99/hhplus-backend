package org.example.lecture.application.service;

import org.example.lecture.application.exception.LectureSlotStatusNotFoundException;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.infrastructure.lecture.LectureSlotStatusRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LectureSlotStatusService {
    private final LectureSlotStatusRepository lectureSlotStatusRepository;

    public LectureSlotStatusService(LectureSlotStatusRepository lectureSlotStatusRepository) {
        this.lectureSlotStatusRepository = lectureSlotStatusRepository;
    }

    /**
     * [2. 특강 선택 API => 강의 슬롯 상태 조회]
     * - 특정 Slot ID에 해당하는 강의 슬롯 상태를 반환.
     * - 상태가 존재하지 않으면 LectureSlotStatusNotFoundException 발생.
     */
    public LectureSlotStatus getSlotStatusBySlotIdWithLock(Long slotId) {
        return lectureSlotStatusRepository.findBySlotIdWithPessimisticLock(slotId)
                .orElseThrow(() -> new LectureSlotStatusNotFoundException(slotId));
    }
}

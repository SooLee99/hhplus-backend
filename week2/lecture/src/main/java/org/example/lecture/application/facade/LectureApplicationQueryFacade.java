package org.example.lecture.application.facade;

import org.example.lecture.application.service.LectureSlotService;
import org.example.lecture.application.service.LectureSlotStatusService;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [강의 슬롯 관리 퍼사드]
 * - ManageLectureSlotUsecase를 호출하여 강의 슬롯의 상태 관리 및 신청자, 대기자 수 관리를 수행.
 */
@Service
public class LectureApplicationQueryFacade {

    private final LectureSlotService lectureSlotService;
    private final LectureSlotStatusService lectureSlotStatusService;

    public LectureApplicationQueryFacade(LectureSlotService lectureSlotService, LectureSlotStatusService lectureSlotStatusService) {
        this.lectureSlotService = lectureSlotService;
        this.lectureSlotStatusService = lectureSlotStatusService;
    }


}

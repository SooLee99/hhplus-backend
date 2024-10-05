package org.example.lecture.application.usecase;

import jakarta.transaction.Transactional;
import org.example.lecture.application.service.ApplicationService;
import org.example.lecture.application.service.LectureSlotService;
import org.example.lecture.domain.application.Application;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.interfaces.dto.LectureApplicationResponseDTO;
import org.springframework.stereotype.Service;

/**
 * - 강의 신청을 수행하고, 중복 신청 방지, 강의 상태 확인, 정원 관리 등을 담당.
 */
@Service
public class ApplyLectureUsecase {

    private final ApplicationService applicationService;
    private final LectureSlotService lectureSlotService;

    public ApplyLectureUsecase(ApplicationService applicationService, LectureSlotService lectureSlotService) {
        this.applicationService = applicationService;
        this.lectureSlotService = lectureSlotService;
    }

    @Transactional
    public LectureApplicationResponseDTO execute(Long userId, Long lectureSlotId) {
        // 1. 강의 슬롯을 비관적 락으로 예약하여 동시성 문제 방지
        LectureSlot lectureSlot = lectureSlotService.reserveSlotWithLock(lectureSlotId);

        // 2. LectureApplicationService를 통해 신청 처리 및 상태 업데이트
        Application application = applicationService.applyToLectureSlot(userId, lectureSlot);


        // 성공 응답 DTO 생성
        return LectureApplicationResponseDTO.builder()
                .applicationId(application.getApplicationId())
                .userId(userId)
                .lectureId(lectureSlot.getLecture().getLectureId())
                .lectureName(lectureSlot.getLecture().getName())
                .slotDate(lectureSlot.getDate())
                .status(application.getCurrentStatus().name())
                .message("신청이 성공적으로 완료되었습니다.")
                .build();
    }
}

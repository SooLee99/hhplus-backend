package org.example.lecture.application.facade;

import org.example.lecture.application.usecase.ApplyLectureUsecase;
import org.example.lecture.domain.application.Application;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.interfaces.dto.LectureApplicationResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [ 특강 신청 퍼사드 ]
 * - ApplyLectureUsecase를 호출하여 특정 강의에 대한 신청 로직을 관리
*/
@Service
public class LectureApplicationFacade {
    private final ApplyLectureUsecase applyLectureUsecase;

    public LectureApplicationFacade(ApplyLectureUsecase applyLectureUsecase) {
        this.applyLectureUsecase = applyLectureUsecase;
    }

    //
    public LectureApplicationResponseDTO applyToLecture(Long userId, Long lectureSlotId) {
        return applyLectureUsecase.execute(userId, lectureSlotId);
    }

}

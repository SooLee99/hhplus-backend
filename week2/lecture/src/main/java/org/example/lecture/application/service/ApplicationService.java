package org.example.lecture.application.service;

import org.example.lecture.application.exception.ApplicationNotFoundException;
import org.example.lecture.domain.application.Application;
import org.example.lecture.domain.application.ApplicationStatusType;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.infrastructure.application.ApplicationRepository;
import org.example.lecture.infrastructure.lecture.LectureSlotRepository;
import org.example.lecture.infrastructure.lecture.LectureSlotStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * 특정 사용자의 신청 완료된 강의 목록을 조회
     * @param userId 사용자 ID
     * @return 신청 완료된 Application 목록
     * @throws ApplicationNotFoundException 사용자의 신청 내역이 없을 때 예외 발생
     */
    public List<Application> getCompletedApplicationsByUserId(Long userId) {
        List<Application> applications = applicationRepository.findByUserIdAndCurrentStatus(userId, ApplicationStatusType.APPLIED);

        if (applications.isEmpty()) {
            throw new ApplicationNotFoundException("해당 사용자 (" + userId + ")의 신청 완료된 강의 내역이 존재하지 않습니다.");
        }

        return applications;
    }

    /**
     * [강의 신청 처리 및 상태 저장]
     * - 강의 슬롯에 대한 신청 상태를 저장.
     * @param userId       신청자 ID
     * @param lectureSlot  신청할 강의 슬롯
     */
    @Transactional
    public Application applyToLectureSlot(Long userId, LectureSlot lectureSlot) {
        // 동일 사용자가 동일 강의에 중복 신청하지 않도록 체크
        if (applicationRepository.existsByUserIdAndLectureSlot(userId, lectureSlot)) {
            throw new IllegalStateException("이미 해당 강의에 신청되었습니다.");
        }

        // 신청 엔티티 생성 및 저장
        Application application = new Application(userId, lectureSlot);

        // 강의 슬롯의 정원이 다 차지 않았을 경우, 상태를 신청 완료로 변경
        application.apply();

        return applicationRepository.save(application);
    }

}
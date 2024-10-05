package org.example.lecture.application.usecase;

import org.example.lecture.application.exception.ApplicationNotFoundException;
import org.example.lecture.application.service.ApplicationService;
import org.example.lecture.domain.application.Application;
import org.example.lecture.interfaces.dto.UserApplicationResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * - 특정 사용자의 신청 내역을 조회하고, 각 신청 내역에 대한 정보를 반환.
 */
@Service
public class QueryUserCompletedApplicationsUsecase {

    private final ApplicationService applicationService;

    public QueryUserCompletedApplicationsUsecase(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * 특정 사용자의 신청 완료된 강의 목록을 조회하고, UserApplicationResponseDTO 리스트로 반환.
     * @param userId 조회할 사용자 ID
     * @return UserApplicationResponseDTO 리스트
     * @throws ApplicationNotFoundException 사용자의 신청 내역이 없을 때 예외 발생
     */
    @Transactional(readOnly = true)
    public List<UserApplicationResponseDTO> execute(Long userId) {
        try {
            // 사용자의 신청 내역을 조회
            List<Application> applications = applicationService.getCompletedApplicationsByUserId(userId);

            // 신청 내역을 DTO로 변환하여 반환
            return applications.stream()
                    .map(application -> UserApplicationResponseDTO.builder()
                            .applicationId(application.getApplicationId())
                            .lectureId(application.getLectureSlot().getLecture().getLectureId())
                            .lectureName(application.getLectureSlot().getLecture().getName())
                            .instructor(application.getLectureSlot().getLecture().getInstructor())
                            .description(application.getLectureSlot().getLecture().getDescription())
                            .slotDate(application.getLectureSlot().getDate())
                            .applicationDate(application.getCreatedAt())
                            .currentStatus(application.getCurrentStatus().name())  // 상태 값
                            .build())
                    .collect(Collectors.toList());
        } catch (ApplicationNotFoundException ex) {
            throw new ApplicationNotFoundException("신청 완료된 강의 목록을 찾을 수 없습니다. 사용자 ID: " + userId);
        }
    }
}

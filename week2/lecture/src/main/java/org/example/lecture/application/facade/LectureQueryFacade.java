package org.example.lecture.application.facade;

import org.example.lecture.application.exception.ApplicationNotFoundException;
import org.example.lecture.application.usecase.QueryLectureDetailsUsecase;
import org.example.lecture.application.usecase.QueryUserCompletedApplicationsUsecase;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.infrastructure.lecture.LectureSlotRepository;
import org.example.lecture.infrastructure.lecture.LectureSlotStatusRepository;
import org.example.lecture.interfaces.dto.LectureResponseDTO;
import org.example.lecture.interfaces.dto.UserApplicationResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [ 강의 조회 퍼사드 ]
 * - QueryLectureDetailsUsecase, QueryUserApplicationsUsecase 등을 호출하여 강의와 신청 내역 조회를 담당.
 */
@Service
public class LectureQueryFacade {
    private final LectureSlotRepository lectureSlotRepository;
    private final LectureSlotStatusRepository lectureSlotStatusRepository;
    private final QueryLectureDetailsUsecase queryLectureDetailsUsecase;
    private final QueryUserCompletedApplicationsUsecase queryUserCompletedApplicationsUsecase;

    public LectureQueryFacade(LectureSlotRepository lectureSlotRepository, LectureSlotStatusRepository lectureSlotStatusRepository, QueryLectureDetailsUsecase queryLectureDetailsUsecase, QueryUserCompletedApplicationsUsecase queryUserCompletedApplicationsUsecase) {
        this.lectureSlotRepository = lectureSlotRepository;
        this.lectureSlotStatusRepository = lectureSlotStatusRepository;
        this.queryLectureDetailsUsecase = queryLectureDetailsUsecase;
        this.queryUserCompletedApplicationsUsecase = queryUserCompletedApplicationsUsecase;
    }

    /**
     * [ 2. 특강 선택 API => 특정 날짜의 신청 가능한 강의 목록 조회 기능]
     * - 날짜별 신청 가능한 강의 목록을 반환합니다.
     * - 각 강의 슬롯의 상태 정보 (OPEN, FULL, CLOSED)를 함께 반환하여, 사용자에게 명확한 신청 가능 여부를 표시합니다.
     */
    public List<LectureResponseDTO> getAvailableLectureSlotsWithStatusByDate(LocalDate date) {
        return queryLectureDetailsUsecase.execute(date);
    }

    /**
     * [ 3. 특강 신청 완료 목록 조회 API => 특정 사용자의 신청 완료 목록 조회]
     * @param userId 사용자 ID
     * @return 신청 완료된 강의 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<UserApplicationResponseDTO> getCompletedApplicationsByUserId(Long userId) {
        try {
            return queryUserCompletedApplicationsUsecase.execute(userId);
        } catch (ApplicationNotFoundException ex) {
            throw new ApplicationNotFoundException("특정 사용자의 신청 내역이 없습니다. 사용자 ID: " + userId);
        }
    }


    /**
     * 모든 강의의 현재 상태를 조회하고, LectureResponseDTO 리스트로 반환
     */
    public List<LectureResponseDTO> getAllLectures() {
        // 모든 강의 슬롯을 조회
        List<LectureSlot> lectureSlots = lectureSlotRepository.findAll();

        return lectureSlots.stream()
                .map(slot -> {
                    // 강의 슬롯의 상태 정보 조회
                    LectureSlotStatus slotStatus = lectureSlotStatusRepository.findByLectureSlot(slot)
                            .orElseThrow(() -> new IllegalArgumentException("강의 슬롯 상태 정보를 찾을 수 없습니다."));

                    // 최신 상태를 반영하여 LectureResponseDTO 생성
                    return LectureResponseDTO.from(slot, slotStatus);
                })
                .collect(Collectors.toList());
    }
}

package org.example.lecture.application.usecase;

import org.example.lecture.application.exception.LectureNotFoundException;
import org.example.lecture.application.service.LectureSlotService;
import org.example.lecture.application.service.LectureSlotStatusService;
import org.example.lecture.domain.lecture.Lecture;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
import org.example.lecture.interfaces.dto.LectureResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * - 특정 강의의 상세 정보를 조회하고, 해당 정보를 반환.
 */
@Service
public class QueryLectureDetailsUsecase {

    private final LectureSlotService lectureSlotService;
    private final LectureSlotStatusService lectureSlotStatusService;


    public QueryLectureDetailsUsecase(LectureSlotService lectureSlotService, LectureSlotStatusService lectureSlotStatusService) {
        this.lectureSlotService = lectureSlotService;
        this.lectureSlotStatusService = lectureSlotStatusService;
    }

    /**
     * [2. 특강 선택 API] => 특정 날짜의 신청 가능한 강의 목록을 반환
     *
     * @param date 조회할 날짜
     * @return 신청 가능한 Lecture 목록
     */
    public List<LectureResponseDTO> execute(LocalDate date) {
        // 특정 날짜의 강의 슬롯 목록 조회
        List<LectureSlot> lectureSlots = lectureSlotService.getLectureSlotsByDate(date);

        // 만약 강의 슬롯이 하나도 없을 경우 예외를 발생시킴
        if (lectureSlots.isEmpty()) {
            throw new LectureNotFoundException("해당 날짜에 조회된 강의 슬롯이 없습니다.");
        }

        // 각 강의 슬롯에 대해 상태 정보(LectureSlotStatus) 조회 및 Lecture 정보를 LectureResponseDTO로 변환하여 반환
        return lectureSlots.stream()
                .map(slot -> {
                    // LectureSlot의 ID로 상태를 조회하고, 상태를 확인
                    LectureSlotStatus slotStatus = lectureSlotStatusService.getSlotStatusBySlotIdWithLock(slot.getSlotId());

                    LectureSlotStatusType status = slotStatus.getStatus();

                    // Lecture 정보 가져오기
                    Lecture lecture = slot.getLecture();

                    // LectureResponseDTO로 변환하여 반환
                    return LectureResponseDTO.builder()
                            .lectureId(lecture.getLectureId())
                            .name(lecture.getName())
                            .instructor(lecture.getInstructor())
                            .description(lecture.getDescription())
                            .maxCapacity(lecture.getMaxCapacity())
                            .slotDate(slot.getDate())  // 슬롯 날짜
                            .capacity(slot.getCapacity())  // 슬롯 최대 수용 가능 인원
                            .status(status)  // 슬롯 상태 (OPEN, FULL, CLOSED)
                            .currentApplicants(slotStatus.getCurrentApplicants())  // 현재 신청자 수
                            .lastUpdatedAt(slot.getUpdatedAt())  // 마지막 업데이트 시간
                            .build();
                })
                .collect(Collectors.toList());
    }

}

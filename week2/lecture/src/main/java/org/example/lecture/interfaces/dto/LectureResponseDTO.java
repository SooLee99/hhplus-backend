package org.example.lecture.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LectureResponseDTO {
    private Long statusId;                  // 상태 ID
    private Long lectureId;                 // 강의 ID
    private String name;                    // 강의 이름
    private String instructor;              // 강사 이름
    private String description;             // 강의 설명
    private int maxCapacity;                // 강의 최대 정원
    private LocalDate slotDate;             // 강의 슬롯 날짜
    private int capacity;                   // 현재 슬롯 정원
    private LectureSlotStatusType status;   // 현재 슬롯 상태 (OPEN, FULL, CLOSED)
    private int currentApplicants;          // 현재 신청자 수
    private LocalDateTime lastUpdatedAt;    // 마지막 업데이트 시간

    // 최신 정보를 반영하여 DTO를 생성하는 정적 팩토리 메서드
    public static LectureResponseDTO from(LectureSlot slot, LectureSlotStatus slotStatus) {
        return LectureResponseDTO.builder()
                .statusId(slotStatus.getStatusId())
                .lectureId(slot.getLecture().getLectureId())
                .name(slot.getLecture().getName())
                .instructor(slot.getLecture().getInstructor())
                .description(slot.getLecture().getDescription())
                .maxCapacity(slot.getLecture().getMaxCapacity())
                .slotDate(slot.getDate())
                .capacity(slot.getCapacity())
                .status(slotStatus.getStatus())
                .currentApplicants(slotStatus.getCurrentApplicants())
                .lastUpdatedAt(slotStatus.getLastUpdatedAt())
                .build();
    }
}

package org.example.lecture.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserApplicationResponseDTO {
    private Long applicationId;               // 신청 ID
    private Long lectureId;                   // 강의 ID
    private String lectureName;               // 강의 이름
    private String instructor;                // 강사 이름
    private String description;               // 강의 설명
    private LocalDate slotDate;               // 강의 진행 날짜
    private LocalDateTime applicationDate;    // 신청 날짜
    private String currentStatus;             // 신청 상태 (APPLIED, WAITING, CANCELED)
}

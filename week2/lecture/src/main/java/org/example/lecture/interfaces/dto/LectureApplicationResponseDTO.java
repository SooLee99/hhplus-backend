package org.example.lecture.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LectureApplicationResponseDTO {

    private Long applicationId;           // 신청 ID
    private Long userId;                  // 사용자 ID
    private Long lectureId;               // 강의 ID
    private String lectureName;           // 강의 이름
    private LocalDate slotDate;           // 신청한 슬롯 날짜
    private String status;                // 신청 상태 (APPLIED, WAITING 등)
    private String message;               // 성공/실패 메시지
}

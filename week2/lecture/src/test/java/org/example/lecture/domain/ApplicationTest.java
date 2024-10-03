package org.example.lecture.domain;

import org.example.lecture.domain.application.Application;
import org.example.lecture.domain.lecture.Lecture;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.application.ApplicationStatusType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Application 도메인 테스트")
public class ApplicationTest {

    @Test
    @DisplayName("Application 상태 취소 테스트 - 신청된 상태에서 취소 시 상태가 CANCELED로 변경된다.")
    void 신청_취소_테스트_Given_신청됨_When_취소_Then_CANCELED_상태() {
        // Given: 신청된 상태의 Application 생성
        Lecture lecture = Lecture.create(1L, "자바 기초", "홍길동", "자바 프로그래밍의 기초", 30);
        LectureSlot lectureSlot = LectureSlot.create(lecture, LocalDate.now(), 30);
        Application application = Application.builder()
                .applicationId(1L)
                .lectureSlot(lectureSlot)
                .userId(1L)
                .currentStatus(ApplicationStatusType.APPLIED)
                .createdAt(LocalDateTime.now())
                .build();

        // When: 신청 취소
        application.cancelApplication();

        // Then: 상태가 CANCELED로 변경되었는지 확인
        assertThat(application.getCurrentStatus()).isEqualTo(ApplicationStatusType.CANCELED);
    }
}

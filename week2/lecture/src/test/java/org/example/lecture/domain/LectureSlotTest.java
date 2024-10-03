package org.example.lecture.domain;

import org.example.lecture.domain.lecture.Lecture;
import org.example.lecture.domain.lecture.LectureSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LectureSlot 도메인 테스트")
public class LectureSlotTest {

    @Test
    @DisplayName("LectureSlot 정원 초과 상태 테스트 - 정원이 0일 때 정원 초과 상태를 반환한다.")
    void 정원_초과_테스트_Given_정원이_0_When_isFull_Then_True_반환() {
        // Given: 정원이 0인 LectureSlot 생성
        Lecture lecture = Lecture.create(1L, "자바 기초", "홍길동", "자바 프로그래밍의 기초", 30);
        LectureSlot lectureSlot = LectureSlot.create(lecture, LocalDate.now(), 0);

        // When: 정원 초과 여부 확인
        boolean isFull = lectureSlot.isFull();

        // Then: 정원이 초과되었음을 확인
        assertThat(isFull).isTrue();
    }

    @Test
    @DisplayName("LectureSlot 정원이 남은 상태 테스트 - 정원이 남아 있을 때 정원 초과 상태가 아니다.")
    void 정원_상태_정상_확인_Given_정원이_남아있을_때_When_isFull_Then_False_반환() {
        // Given: 정원이 10명 남은 LectureSlot 생성
        Lecture lecture = Lecture.create(1L, "고급 자바", "이순신", "자바 심화 과정", 30);
        LectureSlot lectureSlot = LectureSlot.create(lecture, LocalDate.now(), 10);

        // When: 정원 초과 여부 확인
        boolean isFull = lectureSlot.isFull();

        // Then: 정원이 초과되지 않았음을 확인
        assertThat(isFull).isFalse();
    }
}

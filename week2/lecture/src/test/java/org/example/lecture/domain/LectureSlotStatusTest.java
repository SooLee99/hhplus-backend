package org.example.lecture.domain;

import org.example.lecture.domain.lecture.Lecture;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LectureSlotStatus 도메인 테스트")
public class LectureSlotStatusTest {

    @Test
    @DisplayName("신청자 수 증가 테스트 - 상태가 OPEN일 때 신청자 수가 1 증가한다.")
    void 신청자_수_증가_테스트_Given_신청_가능_When_incrementApplicants_Then_신청자_수_증가() {
        // Given: 신청 가능한 상태의 LectureSlotStatus 생성
        Lecture lecture = Lecture.create(1L, "자바 기초", "홍길동", "자바 프로그래밍의 기초", 30);
        LectureSlot lectureSlot = LectureSlot.create(lecture, LocalDate.now(), 30);
        LectureSlotStatus status = new LectureSlotStatus(lectureSlot, LectureSlotStatusType.OPEN, 0, 0, 0);

        // When: 신청자 수 증가
        status.incrementApplicants();

        // Then: 신청자 수가 1 증가했는지 확인
        assertThat(status.getCurrentApplicants()).isEqualTo(1);
        assertThat(status.getStatus()).isEqualTo(LectureSlotStatusType.OPEN);
    }

    @Test
    @DisplayName("정원 초과 상태 변경 테스트 - 신청자가 정원 초과 시 상태가 FULL로 변경된다.")
    void 정원_초과_상태_변경_테스트_Given_정원_초과_When_incrementApplicants_Then_FULL_상태() {
        // Given: 정원이 1명 남은 상태의 LectureSlotStatus 생성
        Lecture lecture = Lecture.create(1L, "자바 기초", "홍길동", "자바 프로그래밍의 기초", 30);
        LectureSlot lectureSlot = LectureSlot.create(lecture, LocalDate.now(), 1);
        LectureSlotStatus status = new LectureSlotStatus(lectureSlot, LectureSlotStatusType.OPEN, 29, 0, 0);

        // When: 신청자 수 증가
        status.incrementApplicants();

        // Then: 상태가 FULL로 변경되었는지 확인
        assertThat(status.getCurrentApplicants()).isEqualTo(30);
        assertThat(status.getStatus()).isEqualTo(LectureSlotStatusType.FULL);
    }

    @Test
    @DisplayName("정원 초과 상태 반환 테스트 - 정원이 초과되었을 때 true를 반환한다.")
    void 정원_초과_상태_반환_테스트_Given_정원_초과_When_isFull_Then_True_반환() {
        // Given: 정원이 초과된 상태의 LectureSlotStatus 생성
        Lecture lecture = Lecture.create(1L, "자바 기초", "홍길동", "자바 프로그래밍의 기초", 30);
        LectureSlot lectureSlot = LectureSlot.create(lecture, LocalDate.now(), 30);
        LectureSlotStatus status = new LectureSlotStatus(lectureSlot, LectureSlotStatusType.FULL, 30, 0, 0);

        // When: 정원 초과 여부 확인
        LectureSlotStatusType isFull = status.getStatus();

        // Then: 정원이 초과되었음을 확인
        assertThat(isFull).isEqualTo(LectureSlotStatusType.FULL);
    }

    @Test
    @DisplayName("대기자 수 증가 테스트 - 대기자 수가 1 증가해야 한다.")
    void 대기자_수_증가_테스트_Given_대기자_When_incrementWaitingList_Then_1_증가() {
        // Given: 대기자 수가 0인 상태의 LectureSlotStatus 생성
        Lecture lecture = Lecture.create(1L, "자바 고급", "홍길동", "자바 심화 과정", 30);
        LectureSlot slot = LectureSlot.create(lecture, LocalDate.now(), 30);
        LectureSlotStatus status = LectureSlotStatus.builder()
                .lectureSlot(slot)
                .status(LectureSlotStatusType.OPEN)
                .currentApplicants(0)
                .waitingList(0)
                .build();

        // When: 대기자 수 증가
        status.incrementWaitingList();

        // Then: 대기자 수가 1 증가했는지 확인
        assertThat(status.getWaitingList()).isEqualTo(1);
    }

    @Test
    @DisplayName("마감 처리 테스트 - 상태가 CLOSED로 변경된다.")
    void 마감_처리_테스트_Given_상태가_변경_When_마감_Then_CLOSED_상태() {
        // Given: OPEN 상태의 LectureSlotStatus 생성
        Lecture lecture = Lecture.create(1L, "자바 고급", "홍길동", "자바 심화 과정", 30);
        LectureSlot slot = LectureSlot.create(lecture, LocalDate.now(), 30);
        LectureSlotStatus status = LectureSlotStatus.builder()
                .lectureSlot(slot)
                .status(LectureSlotStatusType.OPEN)
                .currentApplicants(0)
                .waitingList(0)
                .build();

        // When: 마감 처리
        status.closeRegistration();

        // Then: 상태가 CLOSED로 변경되었는지 확인
        assertThat(status.getStatus()).isEqualTo(LectureSlotStatusType.CLOSED);
    }
}

package org.example.lecture.integration;

import org.example.lecture.application.exception.CapacityExceededException;
import org.example.lecture.application.exception.DuplicateApplicationException;
import org.example.lecture.application.facade.LectureApplicationFacade;
import org.example.lecture.domain.lecture.*;
import org.example.lecture.infrastructure.application.ApplicationRepository;
import org.example.lecture.infrastructure.lecture.LectureRepository;
import org.example.lecture.infrastructure.lecture.LectureSlotRepository;
import org.example.lecture.infrastructure.lecture.LectureSlotStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 동시성 테스트를 통해 40명이 동시에 특강 신청을 시도할 때,
 * 30명만 성공하는 것을 검증하는 통합 테스트 클래스
 */
@SpringBootTest
public class LectureApplicationIntegrationTest {

    @Autowired
    private LectureApplicationFacade lectureApplicationFacade;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private LectureSlotRepository lectureSlotRepository;

    @Autowired
    private LectureSlotStatusRepository lectureSlotStatusRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private LectureSlot lectureSlot;

    @BeforeEach
    public void setUp() {
        // 데이터 정리
        applicationRepository.deleteAll();
        lectureSlotStatusRepository.deleteAll(); // 추가: LectureSlotStatus도 삭제
        lectureSlotRepository.deleteAll();
        lectureRepository.deleteAll();

        // 테스트용 Lecture 생성
        Lecture lecture = Lecture.builder()
                .name("테스트 특강")
                .instructor("테스트 강사")
                .description("테스트 특강 설명")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        lecture = lectureRepository.save(lecture);

        // 테스트용 LectureSlot 생성 (정원 30명)
        lectureSlot = LectureSlot.builder()
                .lecture(lecture)
                .capacity(30)
                .date(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        lectureSlot = lectureSlotRepository.save(lectureSlot);

        // 추가: LectureSlotStatus 생성 및 저장
        LectureSlotStatus lectureSlotStatus = LectureSlotStatus.builder()
                .lectureSlot(lectureSlot)
                .status(LectureSlotStatusType.OPEN)
                .currentApplicants(0)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        lectureSlotStatus = lectureSlotStatusRepository.save(lectureSlotStatus);
    }

    @Test
    @DisplayName("동시에 동일한 특강에 대해 40명이 신청했을 때, 30명만 성공한다")
    public void testConcurrentLectureApplications() throws InterruptedException {
        int numberOfUsers = 40;
        Long lectureSlotId = lectureSlot.getSlotId();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfUsers);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfUsers; i++) {
            final Long userId = Long.valueOf(i);
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    lectureApplicationFacade.applyToLecture(userId, lectureSlotId);
                    return true; // 신청 성공
                } catch (CapacityExceededException e) {
                    return false; // 정원 초과로 신청 실패
                } catch (DuplicateApplicationException e) {
                    return false; // 중복 신청으로 신청 실패
                } catch (Exception e) {
                    e.printStackTrace();
                    return false; // 기타 예외로 신청 실패
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        latch.await(); // 모든 작업이 완료될 때까지 대기
        executorService.shutdown();

        // 신청 결과 집계
        int successCount = 0;
        int failureCount = 0;
        for (Future<Boolean> future : futures) {
            try {
                if (future.get()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (ExecutionException | InterruptedException e) {
                failureCount++;
            }
        }

        // 검증
        assertEquals(30, successCount, "성공한 신청자는 30명이어야 합니다.");
        assertEquals(10, failureCount, "실패한 신청자는 10명이어야 합니다.");

        // 실제 데이터베이스의 신청 내역 확인
        long applicationCount = applicationRepository.count();
        assertEquals(30, applicationCount, "데이터베이스에 저장된 신청 내역은 30건이어야 합니다.");
    }
}

package org.example.lecture.integration;

import org.example.lecture.application.exception.DuplicateApplicationException;
import org.example.lecture.application.facade.LectureApplicationFacade;
import org.example.lecture.domain.lecture.Lecture;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
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

@SpringBootTest
public class DuplicateApplicationIntegrationTest {

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
        // 데이터 초기화
        applicationRepository.deleteAll();
        lectureSlotStatusRepository.deleteAll();
        lectureSlotRepository.deleteAll();
        lectureRepository.deleteAll();

        // 테스트용 Lecture 및 LectureSlot 생성
        Lecture lecture = Lecture.builder()
                .name("테스트 강의")
                .instructor("테스트 강사")
                .description("테스트 강의 설명")
                .maxCapacity(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        lecture = lectureRepository.save(lecture);

        lectureSlot = LectureSlot.builder()
                .lecture(lecture)
                .capacity(30)
                .date(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        lectureSlot = lectureSlotRepository.save(lectureSlot);

        LectureSlotStatus lectureSlotStatus = LectureSlotStatus.builder()
                .lectureSlot(lectureSlot)
                .status(LectureSlotStatusType.OPEN)
                .currentApplicants(0)
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        lectureSlotStatusRepository.save(lectureSlotStatus);
    }

    @Test
    @DisplayName("동일한 사용자가 동일한 강의에 대해 5번 신청했을 때, 1번만 성공한다")
    public void testDuplicateApplicationsBySameUser() throws InterruptedException {
        Long userId = 1L;
        Long lectureSlotId = lectureSlot.getSlotId();

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Future<Boolean> future = executorService.submit(() -> {
                try {
                    lectureApplicationFacade.applyToLecture(userId, lectureSlotId);
                    return true; // 신청 성공
                } catch (DuplicateApplicationException e) {
                    return false; // 중복 신청으로 신청 실패
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
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
        assertEquals(1, successCount, "동일한 사용자는 한 번만 성공해야 합니다.");
        assertEquals(4, failureCount, "나머지 신청은 중복으로 인해 실패해야 합니다.");

        // 데이터베이스에 저장된 신청 내역 확인
        long applicationCount = applicationRepository.count();
        assertEquals(1, applicationCount, "데이터베이스에 저장된 신청 내역은 1건이어야 합니다.");
    }
}

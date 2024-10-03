package org.example.lecture.application.service;

import org.example.lecture.application.exception.ApplicationNotFoundException;
import org.example.lecture.domain.application.Application;
import org.example.lecture.domain.application.ApplicationStatusType;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.infrastructure.application.ApplicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationServiceTest {

    @Test
    @DisplayName("사용자의 신청 완료된 강의 목록을 성공적으로 조회한다")
    public void testGetCompletedApplicationsByUserId_Success() {
        // Given
        Long userId = 1L;
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationService applicationService = new ApplicationService(applicationRepository);

        Application application = mock(Application.class);
        List<Application> applications = List.of(application);

        when(applicationRepository.findByUserIdAndCurrentStatus(userId, ApplicationStatusType.APPLIED))
                .thenReturn(applications);

        // When
        List<Application> result = applicationService.getCompletedApplicationsByUserId(userId);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(applications, result);
    }

    @Test
    @DisplayName("사용자의 신청 완료된 강의 내역이 없을 경우 예외를 발생시킨다")
    public void testGetCompletedApplicationsByUserId_NotFoundException() {
        // Given
        Long userId = 1L;
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationService applicationService = new ApplicationService(applicationRepository);

        when(applicationRepository.findByUserIdAndCurrentStatus(userId, ApplicationStatusType.APPLIED))
                .thenReturn(Collections.emptyList());

        // When & Then
        ApplicationNotFoundException exception = assertThrows(ApplicationNotFoundException.class, () -> {
            applicationService.getCompletedApplicationsByUserId(userId);
        });

        assertEquals("해당 사용자 (" + userId + ")의 신청 완료된 강의 내역이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("강의 슬롯에 대한 신청을 성공적으로 처리한다")
    public void testApplyToLectureSlot_Success() {
        // Given
        Long userId = 1L;
        LectureSlot lectureSlot = mock(LectureSlot.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationService applicationService = new ApplicationService(applicationRepository);

        when(applicationRepository.existsByUserIdAndLectureSlot(userId, lectureSlot))
                .thenReturn(false);
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Application application = applicationService.applyToLectureSlot(userId, lectureSlot);

        // Then
        assertNotNull(application);
        assertEquals(userId, application.getUserId());
        assertEquals(lectureSlot, application.getLectureSlot());
    }

    @Test
    @DisplayName("이미 신청한 강의 슬롯에 다시 신청하려고 하면 예외를 발생시킨다")
    public void testApplyToLectureSlot_AlreadyApplied() {
        // Given
        Long userId = 1L;
        LectureSlot lectureSlot = mock(LectureSlot.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ApplicationService applicationService = new ApplicationService(applicationRepository);

        when(applicationRepository.existsByUserIdAndLectureSlot(userId, lectureSlot))
                .thenReturn(true);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            applicationService.applyToLectureSlot(userId, lectureSlot);
        });

        assertEquals("이미 해당 강의에 신청되었습니다.", exception.getMessage());
    }

}

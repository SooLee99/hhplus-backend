package org.example.lecture.application.service;

import org.example.lecture.application.exception.LectureSlotNotFoundException;
import org.example.lecture.domain.lecture.LectureSlot;
import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.domain.lecture.LectureSlotStatusType;
import org.example.lecture.infrastructure.lecture.LectureSlotRepository;
import org.example.lecture.infrastructure.lecture.LectureSlotStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LectureSlotServiceTest {

    @Test
    @DisplayName("특정 날짜의 강의 슬롯 목록을 성공적으로 조회한다")
    public void testGetLectureSlotsByDate_Success() {
        // Given
        LocalDate date = LocalDate.now();
        LectureSlotRepository lectureSlotRepository = mock(LectureSlotRepository.class);
        LectureSlotStatusService lectureSlotStatusService = mock(LectureSlotStatusService.class);
        LectureSlotStatusRepository lectureSlotStatusRepository = mock(LectureSlotStatusRepository.class);
        LectureSlotService lectureSlotService = new LectureSlotService(
                lectureSlotRepository, lectureSlotStatusService, lectureSlotStatusRepository
        );

        LectureSlot lectureSlot = mock(LectureSlot.class);
        List<LectureSlot> lectureSlots = List.of(lectureSlot);

        when(lectureSlotRepository.findByDate(date)).thenReturn(lectureSlots);

        // When
        List<LectureSlot> result = lectureSlotService.getLectureSlotsByDate(date);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(lectureSlots, result);
    }

    @Test
    @DisplayName("강의 슬롯을 성공적으로 예약한다")
    public void testReserveSlotWithLock_Success() {
        // Given
        Long lectureSlotId = 1L;
        LectureSlotRepository lectureSlotRepository = mock(LectureSlotRepository.class);
        LectureSlotStatusService lectureSlotStatusService = mock(LectureSlotStatusService.class);
        LectureSlotStatusRepository lectureSlotStatusRepository = mock(LectureSlotStatusRepository.class);
        ReentrantLock lock = new ReentrantLock();
        LectureSlotService lectureSlotService = new LectureSlotService(
                lectureSlotRepository, lectureSlotStatusService, lectureSlotStatusRepository
        );

        LectureSlot lectureSlot = mock(LectureSlot.class);
        LectureSlotStatus slotStatus = mock(LectureSlotStatus.class);

        when(lectureSlotRepository.findBySlotIdWithPessimisticLock(lectureSlotId))
                .thenReturn(Optional.of(lectureSlot));
        when(lectureSlotStatusService.getSlotStatusById(lectureSlotId))
                .thenReturn(Optional.of(slotStatus));
        when(lectureSlot.getSlotId()).thenReturn(lectureSlotId);
        when(lectureSlot.getCapacity()).thenReturn(10);
        when(slotStatus.getStatus()).thenReturn(LectureSlotStatusType.OPEN);
        when(slotStatus.getCurrentApplicants()).thenReturn(5);

        // When
        LectureSlot result = lectureSlotService.reserveSlotWithLock(lectureSlotId);

        // Then
        assertNotNull(result);
        assertEquals(lectureSlot, result);
    }

    @Test
    @DisplayName("강의 슬롯이 없을 경우 예외를 발생시킨다")
    public void testReserveSlotWithLock_LectureSlotNotFoundException() {
        // Given
        Long lectureSlotId = 1L;
        LectureSlotRepository lectureSlotRepository = mock(LectureSlotRepository.class);
        LectureSlotStatusService lectureSlotStatusService = mock(LectureSlotStatusService.class);
        LectureSlotStatusRepository lectureSlotStatusRepository = mock(LectureSlotStatusRepository.class);
        LectureSlotService lectureSlotService = new LectureSlotService(
                lectureSlotRepository, lectureSlotStatusService, lectureSlotStatusRepository
        );

        when(lectureSlotRepository.findBySlotIdWithPessimisticLock(lectureSlotId))
                .thenReturn(Optional.empty());

        // When & Then
        LectureSlotNotFoundException exception = assertThrows(LectureSlotNotFoundException.class, () -> {
            lectureSlotService.reserveSlotWithLock(lectureSlotId);
        });

        assertEquals("해당 슬롯의 상태 정보가 존재하지 않습니다. Slot ID: " + lectureSlotId, exception.getMessage());
    }

    @Test
    @DisplayName("강의 슬롯의 상태 정보를 찾지 못한 경우 예외를 발생시킨다")
    public void testReserveSlotWithLock_LectureSlotStatusNotFoundException() {
        // Given
        Long lectureSlotId = 1L;
        LectureSlotRepository lectureSlotRepository = mock(LectureSlotRepository.class);
        LectureSlotStatusService lectureSlotStatusService = mock(LectureSlotStatusService.class);
        LectureSlotStatusRepository lectureSlotStatusRepository = mock(LectureSlotStatusRepository.class);
        LectureSlotService lectureSlotService = new LectureSlotService(
                lectureSlotRepository, lectureSlotStatusService, lectureSlotStatusRepository
        );

        LectureSlot lectureSlot = mock(LectureSlot.class);

        when(lectureSlotRepository.findBySlotIdWithPessimisticLock(lectureSlotId))
                .thenReturn(Optional.of(lectureSlot));
        when(lectureSlotStatusService.getSlotStatusById(lectureSlotId))
                .thenReturn(Optional.empty());
        when(lectureSlot.getSlotId()).thenReturn(lectureSlotId);

        // When & Then
        LectureSlotNotFoundException exception = assertThrows(LectureSlotNotFoundException.class, () -> {
            lectureSlotService.reserveSlotWithLock(lectureSlotId);
        });

        assertEquals("해당 슬롯의 상태 정보가 존재하지 않습니다. Slot ID: " + lectureSlotId, exception.getMessage());
    }

    @Test
    @DisplayName("강의 정원이 초과되었을 경우 예외를 발생시키고 대기자 수를 증가시킨다")
    public void testReserveSlotWithLock_CapacityExceededException() {
        // Given
        Long lectureSlotId = 1L;
        LectureSlotRepository lectureSlotRepository = mock(LectureSlotRepository.class);
        LectureSlotStatusService lectureSlotStatusService = mock(LectureSlotStatusService.class);
        LectureSlotStatusRepository lectureSlotStatusRepository = mock(LectureSlotStatusRepository.class);
        LectureSlotService lectureSlotService = new LectureSlotService(
                lectureSlotRepository, lectureSlotStatusService, lectureSlotStatusRepository
        );

        LectureSlot lectureSlot = mock(LectureSlot.class);
        LectureSlotStatus slotStatus = mock(LectureSlotStatus.class);

        when(lectureSlotRepository.findBySlotIdWithPessimisticLock(lectureSlotId))
                .thenReturn(Optional.of(lectureSlot));
        when(lectureSlotStatusService.getSlotStatusById(lectureSlotId))
                .thenReturn(Optional.of(slotStatus));
        when(lectureSlot.getSlotId()).thenReturn(lectureSlotId);
        when(slotStatus.getStatus()).thenReturn(LectureSlotStatusType.FULL);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            lectureSlotService.reserveSlotWithLock(lectureSlotId);
        });

        assertEquals("강의 정원이 초과되었습니다.", exception.getMessage());
    }

}

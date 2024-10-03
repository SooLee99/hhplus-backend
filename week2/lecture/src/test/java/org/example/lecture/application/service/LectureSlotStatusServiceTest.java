package org.example.lecture.application.service;

import org.example.lecture.domain.lecture.LectureSlotStatus;
import org.example.lecture.infrastructure.lecture.LectureSlotStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LectureSlotStatusServiceTest {

    @Test
    @DisplayName("특정 Slot ID에 해당하는 강의 슬롯 상태를 성공적으로 반환한다")
    public void testGetSlotStatusById_Success() {
        // Given
        Long slotId = 1L;
        LectureSlotStatusRepository lectureSlotStatusRepository = mock(LectureSlotStatusRepository.class);
        LectureSlotStatusService lectureSlotStatusService = new LectureSlotStatusService(lectureSlotStatusRepository);

        LectureSlotStatus lectureSlotStatus = mock(LectureSlotStatus.class);
        when(lectureSlotStatusRepository.findById(slotId)).thenReturn(Optional.of(lectureSlotStatus));

        // When
        Optional<LectureSlotStatus> result = lectureSlotStatusService.getSlotStatusById(slotId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(lectureSlotStatus, result.get());
    }

    @Test
    @DisplayName("존재하지 않는 Slot ID로 강의 슬롯 상태를 조회하면 빈 Optional을 반환한다")
    public void testGetSlotStatusById_NotFound() {
        // Given
        Long slotId = 2L;
        LectureSlotStatusRepository lectureSlotStatusRepository = mock(LectureSlotStatusRepository.class);
        LectureSlotStatusService lectureSlotStatusService = new LectureSlotStatusService(lectureSlotStatusRepository);

        when(lectureSlotStatusRepository.findById(slotId)).thenReturn(Optional.empty());

        // When
        Optional<LectureSlotStatus> result = lectureSlotStatusService.getSlotStatusById(slotId);

        // Then
        assertFalse(result.isPresent());
    }
}

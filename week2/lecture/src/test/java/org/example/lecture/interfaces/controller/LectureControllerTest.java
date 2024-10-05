package org.example.lecture.interfaces.controller;

import org.example.lecture.application.exception.CapacityExceededException;
import org.example.lecture.application.exception.DuplicateApplicationException;
import org.example.lecture.application.facade.LectureApplicationFacade;
import org.example.lecture.application.facade.LectureQueryFacade;
import org.example.lecture.interfaces.dto.LectureApplicationResponseDTO;
import org.example.lecture.interfaces.dto.LectureResponseDTO;
import org.example.lecture.interfaces.dto.UserApplicationResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LectureControllerTest {

    @Test
    @DisplayName("특정 사용자가 특정 강의에 신청을 수행한다")
    public void testApplyToLecture_Success() {
        // Given
        Long userId = 1L;
        Long lectureSlotId = 2L;
        LectureApplicationFacade lectureApplicationFacade = mock(LectureApplicationFacade.class);
        LectureQueryFacade lectureQueryFacade = mock(LectureQueryFacade.class);
        LectureController lectureController = new LectureController(lectureQueryFacade, lectureApplicationFacade);

        LectureApplicationResponseDTO responseDTO = mock(LectureApplicationResponseDTO.class);
        when(lectureApplicationFacade.applyToLecture(userId, lectureSlotId)).thenReturn(responseDTO);

        // When
        ResponseEntity<LectureApplicationResponseDTO> response = lectureController.applyToLecture(userId, lectureSlotId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseDTO, response.getBody());
    }

    @Test
    @DisplayName("동일한 신청자가 동일한 강의에 대해 중복 신청하면 예외를 발생시킨다")
    public void testApplyToLecture_DuplicateApplicationException() {
        // Given
        Long userId = 1L;
        Long lectureSlotId = 2L;
        LectureApplicationFacade lectureApplicationFacade = mock(LectureApplicationFacade.class);
        LectureQueryFacade lectureQueryFacade = mock(LectureQueryFacade.class);
        LectureController lectureController = new LectureController(lectureQueryFacade, lectureApplicationFacade);

        DuplicateApplicationException exception = new DuplicateApplicationException(userId, lectureSlotId);
        doThrow(exception).when(lectureApplicationFacade).applyToLecture(userId, lectureSlotId);

        // When
        ResponseEntity<String> response = lectureController.handleDuplicateApplicationException(exception);

        // Then
        assertNotNull(response);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("사용자 ID: " + userId + "가 이미 강의 ID: " + lectureSlotId + "에 신청했습니다.", response.getBody());
    }

    @Test
    @DisplayName("특강 정원이 초과되면 예외를 발생시킨다")
    public void testApplyToLecture_CapacityExceededException() {
        // Given
        Long userId = 1L;
        Long lectureSlotId = 2L;
        LectureApplicationFacade lectureApplicationFacade = mock(LectureApplicationFacade.class);
        LectureQueryFacade lectureQueryFacade = mock(LectureQueryFacade.class);
        LectureController lectureController = new LectureController(lectureQueryFacade, lectureApplicationFacade);

        CapacityExceededException exception = new CapacityExceededException("정원이 초과되었습니다.");
        doThrow(exception).when(lectureApplicationFacade).applyToLecture(userId, lectureSlotId);

        // When
        ResponseEntity<String> response = lectureController.handleCapacityExceededException(exception);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertEquals("정원이 초과되었습니다.", response.getBody());
    }

    @Test
    @DisplayName("특정 날짜의 강의 슬롯 목록을 성공적으로 조회한다")
    public void testGetAvailableLectureSlotsWithStatusByDate_Success() {
        // Given
        LocalDate date = LocalDate.now();
        LectureApplicationFacade lectureApplicationFacade = mock(LectureApplicationFacade.class);
        LectureQueryFacade lectureQueryFacade = mock(LectureQueryFacade.class);
        LectureController lectureController = new LectureController(lectureQueryFacade, lectureApplicationFacade);

        LectureResponseDTO lectureResponseDTO = mock(LectureResponseDTO.class);
        List<LectureResponseDTO> lectureResponseDTOList = List.of(lectureResponseDTO);
        when(lectureQueryFacade.getAvailableLectureSlotsWithStatusByDate(date)).thenReturn(lectureResponseDTOList);

        // When
        ResponseEntity<List<LectureResponseDTO>> response = lectureController.getAvailableLectureSlotsWithStatusByDate(date);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(lectureResponseDTOList, response.getBody());
    }

    @Test
    @DisplayName("특정 사용자의 신청 완료된 강의 목록을 성공적으로 조회한다")
    public void testGetCompletedLecturesByUserId_Success() {
        // Given
        Long userId = 1L;
        LectureApplicationFacade lectureApplicationFacade = mock(LectureApplicationFacade.class);
        LectureQueryFacade lectureQueryFacade = mock(LectureQueryFacade.class);
        LectureController lectureController = new LectureController(lectureQueryFacade, lectureApplicationFacade);

        UserApplicationResponseDTO userApplicationResponseDTO = mock(UserApplicationResponseDTO.class);
        List<UserApplicationResponseDTO> userApplicationResponseDTOList = List.of(userApplicationResponseDTO);
        when(lectureQueryFacade.getCompletedApplicationsByUserId(userId)).thenReturn(userApplicationResponseDTOList);

        // When
        ResponseEntity<List<UserApplicationResponseDTO>> response = lectureController.getCompletedLecturesByUserId(userId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(userApplicationResponseDTOList, response.getBody());
    }

    @Test
    @DisplayName("모든 강의 정보를 성공적으로 조회한다")
    public void testGetAllLectures_Success() {
        // Given
        LectureApplicationFacade lectureApplicationFacade = mock(LectureApplicationFacade.class);
        LectureQueryFacade lectureQueryFacade = mock(LectureQueryFacade.class);
        LectureController lectureController = new LectureController(lectureQueryFacade, lectureApplicationFacade);

        LectureResponseDTO lectureResponseDTO = mock(LectureResponseDTO.class);
        List<LectureResponseDTO> lectureResponseDTOList = List.of(lectureResponseDTO);
        when(lectureQueryFacade.getAllLectures()).thenReturn(lectureResponseDTOList);

        // When
        ResponseEntity<List<LectureResponseDTO>> response = lectureController.getAllLectures();

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(lectureResponseDTOList, response.getBody());
    }
}

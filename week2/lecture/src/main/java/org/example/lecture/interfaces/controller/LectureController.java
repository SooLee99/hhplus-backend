package org.example.lecture.interfaces.controller;

import org.example.lecture.application.exception.CapacityExceededException;
import org.example.lecture.application.exception.DuplicateApplicationException;
import org.example.lecture.application.facade.LectureApplicationFacade;
import org.example.lecture.application.facade.LectureQueryFacade;
import org.example.lecture.domain.lecture.Lecture;
import org.example.lecture.interfaces.dto.LectureApplicationResponseDTO;
import org.example.lecture.interfaces.dto.LectureResponseDTO;
import org.example.lecture.interfaces.dto.UserApplicationResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/lectures")
public class LectureController {

    private final LectureQueryFacade lectureQueryFacade;
    private final LectureApplicationFacade lectureApplicationFacade;

    public LectureController(LectureQueryFacade lectureQueryFacade, LectureApplicationFacade lectureApplicationFacade) {
        this.lectureQueryFacade = lectureQueryFacade;
        this.lectureApplicationFacade = lectureApplicationFacade;
    }

    /**
     * [1. 특강 신청 API]
     * - 특정 사용자가 특정 강의에 신청을 수행.
     */

    @PostMapping("/slots/{lectureSlotId}/apply")
    public ResponseEntity<LectureApplicationResponseDTO> applyToLecture(@RequestParam Long userId, @PathVariable Long lectureSlotId) {
        LectureApplicationResponseDTO responseDTO = lectureApplicationFacade.applyToLecture(userId, lectureSlotId);
        return ResponseEntity.ok(responseDTO);
    }

    // 예외 처리: 중복 신청
    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<String> handleDuplicateApplicationException(DuplicateApplicationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    // 예외 처리: 정원 초과
    @ExceptionHandler(CapacityExceededException.class)
    public ResponseEntity<String> handleCapacityExceededException(CapacityExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * [2. 특강 선택 API]
     * - 특정 날짜의 강의 슬롯 목록 + 상태 정보 조회
     */
    @GetMapping("/slots/details")
    public ResponseEntity<List<LectureResponseDTO>> getAvailableLectureSlotsWithStatusByDate(@RequestParam("date") LocalDate date) {
        List<LectureResponseDTO> availableSlots = lectureQueryFacade.getAvailableLectureSlotsWithStatusByDate(date);
        return ResponseEntity.ok(availableSlots);
    }

    /**
     * [3. 특정 사용자의 신청 완료된 강의 목록 조회 API]
     * - 사용자의 신청 내역을 조회하고 강의 정보를 반환.
     */
    @GetMapping("/completed/{userId}")
    public ResponseEntity<List<UserApplicationResponseDTO>> getCompletedLecturesByUserId(@PathVariable Long userId) {
        List<UserApplicationResponseDTO> completedLectures = lectureQueryFacade.getCompletedApplicationsByUserId(userId);
        return ResponseEntity.ok(completedLectures);
    }

    /**
     * 요구사항에는 없음.
     * [4. 모든 강의 정보를 조회하는 API]
     * @return 모든 LectureResponseDTO 목록
     */
    @GetMapping("/all")
    public ResponseEntity<List<LectureResponseDTO>> getAllLectures() {
        List<LectureResponseDTO> allLectures = lectureQueryFacade.getAllLectures();
        return ResponseEntity.ok(allLectures);
    }
}

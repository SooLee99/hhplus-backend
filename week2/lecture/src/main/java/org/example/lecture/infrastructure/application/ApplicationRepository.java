package org.example.lecture.infrastructure.application;

import org.example.lecture.domain.application.Application;
import org.example.lecture.domain.application.ApplicationStatusType;
import org.example.lecture.domain.lecture.LectureSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    /**
     * 특정 사용자의 완료된 강의 신청 내역을 조회
     * @param userId 사용자 ID
     * @param status 신청 상태 (APPLIED)
     * @return 해당 사용자 ID와 상태에 맞는 신청 내역 리스트
     */
    List<Application> findByUserIdAndCurrentStatus(Long userId, ApplicationStatusType status);

    boolean existsByUserIdAndLectureSlot(Long userId, LectureSlot lectureSlot);
}

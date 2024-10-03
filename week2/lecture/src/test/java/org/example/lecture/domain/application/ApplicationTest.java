package org.example.lecture.domain.application;

import org.example.lecture.domain.lecture.LectureSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Application 도메인 엔티티에 대한 단위 테스트 클래스
 */
public class ApplicationTest {

    @Test
    @DisplayName("apply 메서드를 호출하면 상태가 신청 상태로 변경된다")
    public void testApplyMethod() {
        // Given
        Long userId = 1L;
        LectureSlot lectureSlot = mock(LectureSlot.class);
        Application application = new Application(userId, lectureSlot);

        // When
        application.apply();

        // Then
        assertEquals(ApplicationStatusType.apply(), application.getCurrentStatus(), "apply 호출 후 상태는 apply이어야 합니다.");
    }
}

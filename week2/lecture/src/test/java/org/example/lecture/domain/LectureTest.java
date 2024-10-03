package org.example.lecture.domain;

import org.example.lecture.domain.lecture.Lecture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Lecture 도메인 테스트")
public class LectureTest {

    @Test
    @DisplayName("Lecture 객체 생성 테스트 - 모든 필드가 올바르게 설정되어야 한다.")
    void 강의_생성_테스트_Given_모든_필드_When_Lecture_생성_Then_정상_생성됨() {
        // Given: 강의 정보 데이터
        String name = "자바 기초";
        String instructor = "홍길동";
        String description = "자바 프로그래밍의 기초";
        int maxCapacity = 30;

        // When: Lecture 객체 생성
        Lecture lecture = Lecture.create(null, name, instructor, description, maxCapacity);

        // Then: 모든 필드가 올바르게 설정되었는지 검증
        assertAll(
                () -> assertThat(lecture.getName()).isEqualTo(name),
                () -> assertThat(lecture.getInstructor()).isEqualTo(instructor),
                () -> assertThat(lecture.getDescription()).isEqualTo(description),
                () -> assertThat(lecture.getMaxCapacity()).isEqualTo(maxCapacity),
                () -> assertThat(lecture.getCreatedAt()).isNotNull(),
                () -> assertThat(lecture.getUpdatedAt()).isNotNull()
        );
    }
}

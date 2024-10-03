-- Lecture 테이블에 예제 강의 데이터 삽입
INSERT INTO lecture (lecture_id, name, instructor, description, max_capacity, created_at, updated_at) VALUES
                                                                                                          (1, 'Java 기초', '홍길동', 'Java 언어의 기초를 배우는 강의입니다.', 30, NOW(), NOW()),
                                                                                                          (2, 'Spring Framework 심화', '이순신', 'Spring Framework를 활용한 웹 애플리케이션 개발 심화 과정.', 30, NOW(), NOW()),
                                                                                                          (3, '데이터베이스 설계 및 최적화', '장보고', '데이터베이스의 기본 개념부터 고급 최적화 기법까지 학습.', 30, NOW(), NOW()),
                                                                                                          (4, 'API 개발 기초', '김유신', 'RESTful API 설계와 구현 기법을 다루는 강의.', 30, NOW(), NOW()),
                                                                                                          (5, '클라우드 컴퓨팅 입문', '유관순', 'AWS, GCP, Azure 클라우드 컴퓨팅 입문 과정.', 30, NOW(), NOW());

-- LectureSlot 테이블에 날짜별 강의 슬롯 데이터 삽입
INSERT INTO lecture_slot (slot_id, lecture_id, date, capacity, created_at, updated_at) VALUES
                                                                                           (1, 1, '2024-10-10', 30, NOW(), NOW()),
                                                                                           (2, 1, '2024-10-15', 30, NOW(), NOW()),
                                                                                           (3, 2, '2024-10-12', 30, NOW(), NOW()),
                                                                                           (4, 2, '2024-10-20', 30, NOW(), NOW()),
                                                                                           (5, 3, '2024-10-14', 30, NOW(), NOW()),
                                                                                           (6, 3, '2024-10-21', 30, NOW(), NOW()),
                                                                                           (7, 4, '2024-10-11', 30, NOW(), NOW()),
                                                                                           (8, 4, '2024-10-22', 30, NOW(), NOW()),
                                                                                           (9, 5, '2024-10-13', 30, NOW(), NOW()),
                                                                                           (10, 5, '2024-10-23', 30, NOW(), NOW());

-- LectureSlotStatus 테이블에 각 슬롯의 상태 데이터 삽입
INSERT INTO lecture_slot_status (status_id, slot_id, status, current_applicants, waiting_list, last_updated_at) VALUES
                                                                                                                    (1, 1, 'OPEN', 10, 0, NOW()),
                                                                                                                    (2, 2, 'OPEN', 20, 0, NOW()),
                                                                                                                    (3, 3, 'FULL', 30, 5, NOW()),
                                                                                                                    (4, 4, 'CLOSED', 30, 0, NOW()),
                                                                                                                    (5, 5, 'OPEN', 5, 0, NOW()),
                                                                                                                    (6, 6, 'OPEN', 25, 0, NOW()),
                                                                                                                    (7, 7, 'OPEN', 10, 0, NOW()),
                                                                                                                    (8, 8, 'CLOSED', 30, 0, NOW()),
                                                                                                                    (9, 9, 'OPEN', 15, 0, NOW()),
                                                                                                                    (10, 10, 'OPEN', 0, 0, NOW());

-- Application 테이블에 신청 내역 데이터 삽입
--INSERT INTO application (application_id, slot_id, user_id, current_status, created_at) VALUES
--                                                                                           (100, 1, 1001, 'APPLIED', NOW()),
--                                                                                           (200, 1, 1002, 'APPLIED', NOW()),
--                                                                                           (300, 2, 1003, 'APPLIED', NOW()),
--                                                                                           (400, 3, 1004, 'APPLIED', NOW()),
--                                                                                           (500, 3, 1005, 'WAITING', NOW()),
--                                                                                           (600, 4, 1006, 'APPLIED', NOW()),
--                                                                                           (700, 5, 1007, 'APPLIED', NOW()),
--                                                                                           (800, 6, 1008, 'APPLIED', NOW()),
--                                                                                           (900, 6, 1009, 'APPLIED', NOW()),
--                                                                                           (1000, 7, 1010, 'APPLIED', NOW());

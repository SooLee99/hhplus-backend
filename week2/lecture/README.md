# 강의 신청 시스템 - 데이터베이스 설계 문서

## 목차

1. [ERD(Entity-Relationship Diagram)](#erdentity-relationship-diagram)
2. [데이터베이스 설계 및 이유](#데이터베이스-설계-및-이유)
    - [Lecture (강의)](#lecture-강의)
    - [LectureSlot (강의 슬롯)](#lectureslot-강의-슬롯)
    - [LectureSlotStatus (강의 슬롯 상태)](#lectureslotstatus-강의-슬롯-상태)
    - [Application (강의 신청)](#application-강의-신청)
3. [설계 고려 사항](#설계-고려-사항)
    - [읽기/쓰기 분리 전략](#읽기쓰기-분리-전략)
    - [동시성 처리](#동시성-처리)
    - [성능 최적화](#성능-최적화)
   
---

## ERD(Entity-Relationship Diagram)

아래는 강의 신청 시스템을 표현하는 ERD의 텍스트 형식입니다:

```
+----------------+            +-------------------+          +------------------------+
|    Lecture     |1          *|    LectureSlot    |1        1|  LectureSlotStatus     |
|----------------|------------|-------------------|----------|------------------------|
| lecture_id PK  |            | slot_id PK        |          | status_id PK           |
| name           |            | lecture_id FK     |          | slot_id FK             |
| instructor     |            | date              |          | status                 |
| description    |            | capacity          |          | current_applicants     |
| max_capacity   |            | created_at        |          | last_updated_at        |
| created_at     |            | updated_at        |          |                        |
| updated_at     |            |                   |          |                        |
+----------------+            +-------------------+          +------------------------+
          |                              |1
          |                              |
          |                              |
          |                              |
          |1                            *|
+----------------+            +-------------------+
|  Application   |            |    User           |
|----------------|            |-------------------|
| application_id |            | user_id PK        |
| slot_id FK     |            | ...               |
| user_id        |            |                   |
| current_status |            |                   |
| created_at     |            |                   |
+----------------+            +-------------------+
```

**Legend:**

- **PK**: 기본 키
- **FK**: 외래 키
- **1**: 1
- ***:** 다수

---

## 데이터베이스 설계 및 이유

### Lecture (강의)

- **테이블명:** `lecture`
- **역할:** 각 강의의 기본 정보와 메타데이터를 저장합니다.

**필드 설명:**

- `lecture_id` (PK): 각 강의를 고유하게 식별하는 기본 키입니다.
- `name`: 강의 이름.
- `instructor`: 강의를 진행하는 강사 이름.
- `description`: 강의에 대한 상세 설명.
- `max_capacity`: 해당 강의의 최대 수용 인원.
- `created_at`: 레코드가 생성된 시간.
- `updated_at`: 레코드가 마지막으로 업데이트된 시간.

**설계 이유:**

- 강의의 기본 정보는 변경되지 않는 **읽기 전용 엔티티**로 설계하였습니다.
- 주로 강의 목록 조회 시 사용되며, 강의 슬롯과의 관계를 통해 특정 강의의 슬롯을 참조할 수 있습니다.

### LectureSlot (강의 슬롯)

- **테이블명:** `lecture_slot`
- **역할:** 특정 날짜에 진행되는 강의의 슬롯 정보를 관리합니다.

**필드 설명:**

- `slot_id` (PK): 각 강의 슬롯의 고유 식별자입니다.
- `lecture_id` (FK): 해당 슬롯이 어떤 강의에 속하는지 나타내는 외래 키.
- `date`: 강의가 진행되는 날짜.
- `capacity`: 해당 날짜의 수용 가능 인원.
- `created_at`: 슬롯이 생성된 시간.
- `updated_at`: 슬롯이 마지막으로 업데이트된 시간.

**설계 이유:**

- **Lecture**와 **LectureSlot**을 분리하여 특정 날짜별 강의 진행 정보를 효율적으로 관리합니다.
- 동일 강의가 여러 날짜에 걸쳐 열릴 수 있기 때문에, 강의별로 날짜를 구분하여 저장합니다.

### LectureSlotStatus (강의 슬롯 상태)

- **테이블명:** `lecture_slot_status`
- **역할:** 강의 슬롯의 상태 정보와 현재 신청자 수를 관리합니다.

**필드 설명:**

- `status_id` (PK): 슬롯 상태의 고유 식별자.
- `slot_id` (FK): 강의 슬롯을 참조하는 외래 키.
- `status`: 현재 슬롯의 상태 (e.g., OPEN, FULL).
- `current_applicants`: 현재 신청자 수.
- `last_updated_at`: 마지막으로 상태가 업데이트된 시간.

**설계 이유:**

- **쓰기 전용 엔티티**로 상태 변화를 관리합니다.
- `LectureSlot`의 상태를 분리하여 상태 변화를 빠르고 효율적으로 처리할 수 있도록 설계하였습니다.

### Application (강의 신청)

- **테이블명:** `application`
- **역할:** 각 사용자별 강의 신청 내역을 관리합니다.

**필드 설명:**

- `application_id` (PK): 강의 신청의 고유 식별자.
- `slot_id` (FK): 신청이 이루어진 강의 슬롯을 나타내는 외래 키.
- `user_id`: 신청한 사용자의 ID.
- `current_status`: 현재 신청 상태 (e.g., APPLIED, CANCELED).
- `created_at`: 신청이 이루어진 시간.

**설계 이유:**

- 각 사용자가 특정 강의 슬롯에 신청한 내역을 관리하고, 중복 신청을 방지하기 위해 `user_id`와 `slot_id`의 **유일성 제약 조건**을 적용합니다.
- 특강 신청 내역을 조회할 때 사용되며, 변경되지 않는 읽기 전용 엔티티로 설계하였습니다.
---

## 설계 고려 사항

### 읽기/쓰기 분리 전략

- **읽기 전용 엔티티:** `Lecture`와 `LectureSlot`은 대부분 조회만 이루어지므로 읽기 전용으로 설정하였습니다.
- **쓰기 전용 엔티티:** `LectureSlotStatus`와 `Application`은 빈번하게 상태가 변경되므로, 쓰기 전용 엔티티로 설정하였습니다.

### 동시성 처리

- **Pessimistic Locking (비관적 잠금):**
    - 동시성 문제가 발생할 수 있는 `LectureSlotStatus`에 대해 비관적 잠금을 적용하여 동시에 다수의 사용자가 접근할 때도 데이터의 일관성을 유지합니다.

- **유일성 제약 조건:**
    - `Application` 테이블의 `user_id`와 `slot_id`에 대한 유일성 제약을 추가하여 동일 사용자가 동일한 슬롯에 여러 번 신청하지 않도록 방지합니다.

### 성능 최적화

- **지연 로딩(Lazy Loading):**
    - `LectureSlot`과 `Lecture`의 관계에서 `FetchType.LAZY`를 사용하여 필요할 때만 데이터를 로드합니다.
    - 메모리 사용량을 줄이고 초기 로딩 시간을 단축합니다.

- **인덱스 및 키 설정:**
    - 기본 키 및 외래 키 필드에 인덱스를 설정하여 조회 성능을 최적화합니다.

---

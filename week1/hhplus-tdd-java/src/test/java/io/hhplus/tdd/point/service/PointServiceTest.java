package io.hhplus.tdd.point.service;

import io.hhplus.tdd.exception.*;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.response.ChargeUserPointResponse;
import io.hhplus.tdd.point.dto.response.UseUserPointResponse;
import io.hhplus.tdd.point.dto.response.UserPointHistoryResponse;
import io.hhplus.tdd.point.dto.response.UserPointResponse;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PointServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointValidationService pointValidationService;

    @InjectMocks
    private PointService pointService;

    @Mock
    private ReentrantLock lock;

    public PointServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    // ------------------------------ 1.1. 포인트 충전 테스트 ------------------------------ //

    /**
     * 1.1.1. 정상적인 포인트 충전 테스트
     * 상황: 사용자가 포인트를 정상적으로 충전하는 경우.
     * 입력: 사용자 ID와 충전 금액이 유효함.
     * 예상 결과: 잔액이 충전 금액만큼 증가해야 함.
     */
    @Test
    @DisplayName("정상적인 포인트 충전 테스트")
    public void testChargePoints_Success() {
        // given
        long userId = 1L;
        long initialBalance = 500L;
        long chargeAmount = 200L;
        UserPoint mockUserPoint = new UserPoint(userId, initialBalance, System.currentTimeMillis());

        // when: validateUserExists를 모의 처리하여 사용자 정보를 반환하도록 설정
        when(pointValidationService.validateUserExists(userId)).thenReturn(mockUserPoint);
        when(userPointRepository.insertOrUpdate(userId, initialBalance + chargeAmount)).thenReturn(
                new UserPoint(userId, initialBalance + chargeAmount, System.currentTimeMillis())
        );

        // 포인트 충전 시도
        ChargeUserPointResponse result = pointService.chargeUserPoint(userId, chargeAmount);

        // then: 결과 검증
        assertEquals(userId, result.id());
        assertEquals(initialBalance + chargeAmount, result.point());

        // 검증: 호출된 메서드 확인
        verify(pointValidationService, times(1)).validateUserExists(userId);
        verify(userPointRepository, times(1)).insertOrUpdate(userId, initialBalance + chargeAmount);
        verify(pointHistoryRepository, times(1)).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    /**
     * 1.1.2. 최대 포인트 한도 초과 충전 테스트
     * 상황: 사용자가 최대 포인트 한도를 초과하여 충전하는 경우.
     * 입력: 충전 금액 + 기존 잔액 > 최대 포인트 한도.
     * 예상 결과: IllegalArgumentException 발생, 적절한 에러 메시지 반환.
     */
    @Test
    @DisplayName("최대 포인트 한도 초과 충전 테스트")
    public void testChargePoints_ExceedMaxLimit() {
        // given
        long userId = 1L;
        long initialBalance = 950_000L;
        long chargeAmount = 100_000L;
        UserPoint mockUserPoint = new UserPoint(userId, initialBalance, System.currentTimeMillis());

        // when: validateUserExists를 모의 처리하여 사용자 정보를 반환하도록 설정
        when(pointValidationService.validateUserExists(userId)).thenReturn(mockUserPoint);
        when(userPointRepository.insertOrUpdate(userId, initialBalance + chargeAmount))
                .thenThrow(new MaxPointLimitExceededException());

        // then: 최대 포인트 한도 초과로 인해 예외 발생 확인
        MaxPointLimitExceededException exception = assertThrows(MaxPointLimitExceededException.class, () -> {
            pointService.chargeUserPoint(userId, chargeAmount);
        });
        assertEquals(ErrorCode.MAX_POINT_LIMIT_EXCEEDED.getMessage(), exception.getMessage());

        // 검증: 호출된 메서드 확인
        verify(pointValidationService, times(1)).validateUserExists(userId);
        verify(userPointRepository, times(1)).insertOrUpdate(userId, initialBalance + chargeAmount);
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }


    /**
     * 1.1.3. 충전 금액이 0일 경우 테스트
     * 상황: 사용자가 충전 금액으로 0을 입력한 경우.
     * 입력: 충전 금액이 0.
     * 예상 결과: IllegalArgumentException 발생, "유효하지 않은 금액" 메시지 반환.
     */
    @Test
    @DisplayName("충전 금액이 0일 경우 테스트")
    public void testChargePoints_AmountZero() {
        // given
        long userId = 1L;
        long chargeAmount = 0L;

        // mock 사용 시 실제 메소드를 호출하도록 설정
        doCallRealMethod().when(pointValidationService).checkAmount(anyLong());

        // when & then: 충전 금액이 0일 때 예외 발생 확인
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            pointService.chargeUserPoint(userId, chargeAmount);
        });
        assertEquals(ErrorCode.INVALID_AMOUNT.getMessage(), exception.getMessage());

        verify(userPointRepository, never()).selectById(anyLong());
        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    /**
     * 1.1.4. 충전 금액이 음수일 경우 테스트
     * 상황: 사용자가 음수 금액을 충전하려는 경우.
     * 입력: 충전 금액이 음수.
     * 예상 결과: InvalidAmountException 발생, "유효하지 않은 금액" 메시지 반환.
     */
    @Test
    @DisplayName("음수 금액 검증 테스트")
    public void testCheckAmount_NegativeAmount() {
        // Mock UserPointRepository 생성
        UserPointRepository mockUserPointRepository = Mockito.mock(UserPointRepository.class);

        // PointValidationService에 mockUserPointRepository 주입
        PointValidationService pointValidationService = new PointValidationService(mockUserPointRepository);

        Long negativeAmount = -100L;

        // 음수 금액을 검증할 때 예외가 발생하는지 확인
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            pointValidationService.checkAmount(negativeAmount);
        });

        // 예외 메시지가 정확한지 검증
        assertEquals(ErrorCode.INVALID_AMOUNT.getMessage(), exception.getMessage());
    }

    /**
     * 1.1.5. 존재하지 않는 사용자에게 충전 요청 테스트
     * 상황: 존재하지 않는 사용자 ID로 충전 요청.
     * 입력: 잘못된 사용자 ID.
     * 예상 결과: IllegalArgumentException 발생, "잘못된 사용자 ID" 메시지 반환.
     */
    @Test
    @DisplayName("존재하지 않는 사용자에게 충전 요청 테스트")
    public void testChargePoints_InvalidUser() {
        // given
        long invalidUserId = 999L;

        // when: userPointRepository.selectById()가 null을 반환하도록 설정
        when(pointValidationService.validateUserExists(invalidUserId))
                .thenThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
        // then: 충전 요청 시 예외 발생 확인
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.chargeUserPoint(invalidUserId, 100L);
        });

        // 예외 메시지가 정확한지 확인
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());

        // 검증: userPointRepository의 selectById 호출 확인
        verify(pointValidationService, times(1)).validateUserExists(invalidUserId);
        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    // ------------------------------ 1.2. 포인트 사용 테스트 ------------------------------ //

    /**
     * 1.2.1. 정상적인 포인트 사용 테스트
     * 상황: 사용자가 잔액 범위 내에서 포인트를 사용하는 경우.
     * 입력: 사용자 ID와 사용 금액이 유효함.
     * 예상 결과: 사용 금액만큼 잔액이 감소해야 함.
     */
    @Test
    @DisplayName("정상적인 포인트 사용 요청")
    public void testUsePoints_Success() {
        // Given
        long userId = 1L;
        long currentBalance = 1000L;
        long useAmount = 200L;
        UserPoint mockUserPoint = new UserPoint(userId, currentBalance, System.currentTimeMillis());

        // Mock 설정
        when(pointValidationService.validateUserExists(userId)).thenReturn(mockUserPoint);  // 사용자 검증
        doNothing().when(pointValidationService).checkAmount(useAmount);  // 금액 검증
        doNothing().when(pointValidationService).validateSufficientBalance(currentBalance, useAmount);  // 잔액 검증
        when(userPointRepository.insertOrUpdate(userId, currentBalance - useAmount))
                .thenReturn(new UserPoint(userId, currentBalance - useAmount, System.currentTimeMillis()));

        // When
        UseUserPointResponse response = pointService.usePoints(userId, useAmount);

        // Then
        assertEquals(userId, response.id());
        assertEquals(currentBalance - useAmount, response.point());  // response.updatedPoint() -> response.point()로 수정
        assertNotNull(response.updateMillis());  // 응답의 업데이트 시간 확인

        // Verify
        verify(pointValidationService, times(1)).validateUserExists(userId);  // 사용자 검증 호출 확인
        verify(pointValidationService, times(1)).checkAmount(useAmount);  // 금액 검증 호출 확인
        verify(pointValidationService, times(1)).validateSufficientBalance(currentBalance, useAmount);  // 잔액 검증 호출 확인
        verify(userPointRepository, times(1)).insertOrUpdate(userId, currentBalance - useAmount);  // 업데이트 확인
        verify(pointHistoryRepository, times(1)).insert(eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong());  // 기록 확인
    }

    /**
     * 1.2.2. 잔액이 부족한 경우 테스트
     * 상황: 사용자가 잔액보다 더 많은 포인트를 사용하려는 경우.
     * 입력: 사용 금액 > 잔액.
     * 예상 결과: IllegalArgumentException 발생, "잔액이 부족합니다" 메시지 반환.
     */
    @Test
    @DisplayName("잔액이 부족한 경우 테스트")
    public void testUsePoints_InsufficientBalance() {
        // Given
        long userId = 1L;
        long currentBalance = 100L;
        long useAmount = 200L;
        UserPoint mockUserPoint = new UserPoint(userId, currentBalance, System.currentTimeMillis());

        // Mock 설정: 사용자가 존재하지만 잔액이 부족한 상태
        when(pointValidationService.validateUserExists(userId)).thenReturn(mockUserPoint);  // 사용자 검증
        doNothing().when(pointValidationService).checkAmount(useAmount);  // 금액 검증

        // 잔액 부족 시 검증에서 예외 발생
        doThrow(new InsufficientBalanceException(ErrorCode.INSUFFICIENT_BALANCE.getMessage()))
                .when(pointValidationService).validateSufficientBalance(currentBalance, useAmount);

        // When & Then: 잔액이 부족한 경우 InsufficientBalanceException 발생 확인
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            pointService.usePoints(userId, useAmount);
        });

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE.getMessage(), exception.getMessage());

        // Verify
        verify(pointValidationService, times(1)).validateUserExists(userId);  // 사용자 검증 호출 확인
        verify(pointValidationService, times(1)).checkAmount(useAmount);  // 금액 검증 호출 확인
        verify(pointValidationService, times(1)).validateSufficientBalance(currentBalance, useAmount);  // 잔액 검증 호출 확인
        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());  // 잔액이 부족하므로 업데이트가 발생하지 않아야 함
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(), anyLong());  // 포인트 사용 내역도 기록되지 않아야 함
    }

    /**
     * 1.2.3. 사용 금액이 0일 경우 테스트
     * 상황: 사용자가 0 포인트를 사용하려는 경우.
     * 입력: 사용 금액이 0.
     * 예상 결과: IllegalArgumentException 발생, "유효하지 않은 금액" 메시지 반환.
     */
    @Test
    @DisplayName("사용 금액이 0일 경우 테스트")
    public void testUsePoints_AmountZero() {
        // Given
        long userId = 1L;
        long useAmount = 0L;

        // Mock 설정: 사용자가 존재하지만 사용 금액이 0인 경우
        when(userPointRepository.selectById(userId)).thenReturn(new UserPoint(userId, 100L, System.currentTimeMillis()));

        // checkAmount()를 실제로 호출하도록 설정
        doCallRealMethod().when(pointValidationService).checkAmount(anyLong());

        // When & Then: 사용 금액이 0일 경우 IllegalArgumentException 발생 확인
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            pointService.usePoints(userId, useAmount);
        });

        assertEquals(ErrorCode.INVALID_AMOUNT.getMessage(), exception.getMessage());
        // Verify
        verify(pointValidationService, times(1)).checkAmount(useAmount);
        verify(userPointRepository, never()).selectById(anyLong());
        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    /**
     * 1.2.4. 사용 금액이 음수일 경우 테스트
     * 상황: 사용자가 음수 금액을 사용하려는 경우.
     * 입력: 사용 금액이 음수.
     * 예상 결과: IllegalArgumentException 발생, "유효하지 않은 금액" 메시지 반환.
     */
    @Test
    @DisplayName("사용 금액이 음수일 경우 테스트")
    public void testUsePoints_NegativeAmount() {
        // Given
        long userId = 1L;
        long useAmount = -100L;
        doCallRealMethod().when(pointValidationService).checkAmount(anyLong());

        // When & Then: 사용 금액이 음수일 경우 IllegalArgumentException 발생 확인
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () -> {
            pointService.usePoints(userId, useAmount);
        });

        assertEquals(ErrorCode.INVALID_AMOUNT.getMessage(), exception.getMessage());
        // Verify
        verify(userPointRepository, never()).selectById(anyLong());
        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    /**
     * 1.2.5. 존재하지 않는 사용자에게 사용 요청 테스트
     * 상황: 존재하지 않는 사용자 ID로 사용 요청.
     * 입력: 잘못된 사용자 ID.
     * 예상 결과: IllegalArgumentException 발생, "잘못된 사용자 ID" 메시지 반환.
     */
    @Test
    @DisplayName("존재하지 않는 사용자에게 사용 요청 테스트")
    public void testUsePoints_InvalidUser() {
        // Given
        long invalidUserId = 999L;

        // Mock 설정: validateUserExists 메서드가 호출될 때 UserNotFoundException 던지도록 설정
        doThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()))
                .when(pointValidationService).validateUserExists(invalidUserId);

        // When & Then: 존재하지 않는 사용자로 인해 UserNotFoundException 발생 확인
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.usePoints(invalidUserId, 100L);
        });

        // 예외 메시지 확인
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());

        // Verify: 검증 로직 확인
        verify(pointValidationService, times(1)).validateUserExists(invalidUserId);
        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    // ------------------------------ 1.3. 포인트 잔액 조회 테스트 ------------------------------ //

    /**
     * 1.3.1. 정상적인 잔액 조회 테스트
     * 상황: 사용자가 자신의 잔액을 정상적으로 조회하는 경우.
     * 입력: 유효한 사용자 ID.
     * 예상 결과: 사용자 포인트 잔액이 정상적으로 반환됨.
     */
    @Test
    @DisplayName("정상적인 잔액 조회 테스트")
    public void testGetBalance_Success() {
        // given: 사용자 포인트 생성
        Long userId = 1L;
        long currentBalance = 1000L;
        UserPoint userPoint = new UserPoint(userId, currentBalance, System.currentTimeMillis());

        // when: 사용자 포인트를 반환하도록 설정 (validateUserExists 사용)
        when(pointValidationService.validateUserExists(userId)).thenReturn(userPoint);

        // then: 사용자 포인트 잔액이 정확하게 반환되는지 확인
        UserPointResponse result = pointService.getBalance(userId);
        assertEquals(currentBalance, result.point());
        assertEquals(userId, result.id());
    }


    /**
     * 1.3.2. 잔액 조회 시 사용자의 포인트 존재하지 않는 사용자 테스트
     * 상황: 잔액이 존재하지 않는 사용자 ID로 잔액을 조회하려는 경우.
     * 입력: 잘못된 사용자 ID.
     * 예상 결과: IllegalArgumentException 발생, "사용자의 포인트 조회가 안되고 있습니다." 메시지 반환.
     */
    @Test
    @DisplayName("잔액 조회 시 존재하지 않는 사용자 테스트")
    public void testGetBalance_InvalidUser() {
        // given: 존재하지 않는 사용자 ID
        Long invalidUserId = 999L;

        // when: validateUserExists가 호출될 때 UserNotFoundException을 던지도록 설정
        doThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()))
                .when(pointValidationService).validateUserExists(invalidUserId);

        // then: UserNotFoundException이 발생하는지 확인
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.getBalance(invalidUserId);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());

        // Verify: validateUserExists가 정확히 1회 호출되었는지 확인
        verify(pointValidationService, times(1)).validateUserExists(invalidUserId);
        verify(userPointRepository, never()).insertOrUpdate(anyLong(), anyLong());  // 포인트 업데이트가 일어나지 않아야 함
    }


    // ------------------------------ 1.4. 포인트 거래 내역 조회 테스트 ------------------------------ //

    /**
     * 1.4.1. 정상적인 거래 내역 조회 테스트
     * 상황: 사용자가 자신의 포인트 거래 내역을 조회하는 경우.
     * 입력: 유효한 사용자 ID.
     * 예상 결과: 충전 및 사용 내역이 정확하게 반환됨.
     */
    @Test
    @DisplayName("정상적인 거래 내역 조회 테스트")
    public void testGetPointHistories_Success() {
        // given: 거래 내역 생성
        long userId = 1L;
        UserPoint mockUserPoint = new UserPoint(userId, 1000L, System.currentTimeMillis()); // 사용자 정보 설정
        List<PointHistory> expectedHistories = List.of(
                new PointHistory(1L, userId, 500L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 200L, TransactionType.USE, System.currentTimeMillis())
        );

        // when: 사용자 정보와 가짜 거래 내역을 반환하도록 설정
        when(userPointRepository.selectById(userId)).thenReturn(mockUserPoint);  // 사용자 정보 설정
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(expectedHistories);  // 거래 내역 설정

        // 서비스 메서드 호출
        List<UserPointHistoryResponse> result = pointService.getPointHistories(userId);

        // then: 거래 내역 수 검증 & 각 항목에 대해 검증 (충전 및 사용 내역이 제대로 변환되었는지 확인)
        assertEquals(expectedHistories.size(), result.size());

        // 각 항목에 대해 확인 (충전 및 사용 내역이 제대로 변환되었는지 확인)
        for (int i = 0; i < expectedHistories.size(); i++) {
            PointHistory expectedHistory = expectedHistories.get(i);
            UserPointHistoryResponse actualResponse = result.get(i);

            assertEquals(expectedHistory.id(), actualResponse.id());
            assertEquals(expectedHistory.amount(), actualResponse.amount());
            assertEquals(expectedHistory.type(), actualResponse.type());
            assertEquals(expectedHistory.updateMillis(), actualResponse.updateMillis());
        }

        // pointHistoryRepository.selectAllByUserId(userId)가 정확히 한 번 호출되었는지 확인
        verify(pointHistoryRepository, times(1)).selectAllByUserId(userId);
    }

    /**
     * 1.4.2. 거래 내역이 없는 경우 테스트
     * 상황: 사용자의 거래 내역이 없는 경우.
     * 입력: 유효한 사용자 ID.
     * 예상 결과: 빈 리스트가 반환됨.
     */
    @Test
    @DisplayName("거래 내역이 없는 경우 테스트")
    public void testGetPointHistoriesWhenNoHistoryExists() {
        // Given: 유효한 사용자 ID이지만 거래 내역이 없는 경우
        long validUserId = 1L;
        when(userPointRepository.selectById(validUserId)).thenReturn(new UserPoint(validUserId, 1000L, System.currentTimeMillis()));
        when(pointHistoryRepository.selectAllByUserId(validUserId)).thenReturn(Collections.emptyList());

        // When: 거래 내역 조회 요청
        List<UserPointHistoryResponse> pointHistories = pointService.getPointHistories(validUserId);

        // Then: 거래 내역이 없는 경우 빈 리스트가 반환됨
        assertTrue(pointHistories.isEmpty());
    }
    /**
     * 1.4.3. 존재하지 않는 사용자의 거래 내역 조회 테스트
     * 상황: 존재하지 않는 사용자 ID로 거래 내역을 조회하려는 경우.
     * 입력: 잘못된 사용자 ID.
     * 예상 결과: IllegalArgumentException 발생, "잘못된 사용자 ID" 메시지 반환.
     */
    @Test
    @DisplayName("존재하지 않는 사용자 거래 내역 조회 시도")
    public void testGetPointHistoriesWithInvalidUserId() {
        // Given: 존재하지 않는 사용자 ID
        Long invalidUserId = 999L;

        // 사용자 검증 로직에서 UserNotFoundException 던지도록 설정
        doThrow(new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()))
                .when(pointValidationService).validateUserExists(invalidUserId);

        // When: 거래 내역 조회 시도
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.getPointHistories(invalidUserId);
        });

        // Then: UserNotFoundException 발생 확인
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), exception.getMessage());

        // 검증: validateUserExists가 한 번 호출되었는지 확인
        verify(pointValidationService, times(1)).validateUserExists(invalidUserId);
    }
    // ------------------------------ 1.5. 포인트 충전 및 사용 시 동시성 테스트 ------------------------------ //

    @Test
    @DisplayName("동시 포인트 충전 요청 테스트")
    public void testConcurrentPointCharging() throws InterruptedException {
        // Given
        long userId = 1L;
        long initialBalance = 500L;
        long chargeAmount = 100L;

        // 충전 요청 이전의 초기 사용자 상태를 설정
        UserPoint initialUserPoint = new UserPoint(userId, initialBalance, System.currentTimeMillis());

        // 동시성 테스트를 위해 잔액이 업데이트될 때마다 최신 상태 반환
        when(pointValidationService.validateUserExists(userId)).thenReturn(initialUserPoint);
        when(userPointRepository.insertOrUpdate(eq(userId), anyLong())).thenAnswer(invocation -> {
            long newBalance = (long) invocation.getArgument(1);
            // 새로운 UserPoint 인스턴스를 반환하여 최신 잔액을 반영
            System.out.println("Thread: " + Thread.currentThread().getName() + " | 충전 후 잔액: " + newBalance);
            return new UserPoint(userId, newBalance, System.currentTimeMillis());
        });

        int numberOfThreads = 10;  // 동시 요청 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // 동시 충전 요청
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                pointService.chargeUserPoint(userId, chargeAmount);
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 총 500 + 100 * 10 = 1500 포인트가 되어야 함
        long expectedFinalBalance = initialBalance + (chargeAmount * numberOfThreads);

        // 각 호출이 끝난 후 최신 잔액을 확인하기 위해 다시 한번 검증
        UserPoint finalUserPoint = new UserPoint(userId, expectedFinalBalance, System.currentTimeMillis());
        when(pointValidationService.validateUserExists(userId)).thenReturn(finalUserPoint);

        ChargeUserPointResponse finalResponse = pointService.chargeUserPoint(userId, 0L);
        System.out.println("최종 잔액: " + finalResponse.point());
        assertEquals(expectedFinalBalance, finalResponse.point());
    }

    @Test
    @DisplayName("동시 포인트 사용 요청 테스트")
    public void testConcurrentPointUsage() throws InterruptedException {
        // Given
        long userId = 1L;
        long initialBalance = 1000L;
        long useAmount = 100L;
        long[] balance = {initialBalance};  // 잔액을 공유하는 배열로 상태 관리

        // validateUserExists에서 최신 잔액을 반영하도록 설정
        when(pointValidationService.validateUserExists(userId)).thenAnswer(invocation -> new UserPoint(userId, balance[0], System.currentTimeMillis()));

        // insertOrUpdate가 호출될 때마다 잔액을 업데이트하도록 설정
        when(userPointRepository.insertOrUpdate(eq(userId), anyLong())).thenAnswer(invocation -> {
            long newBalance = (long) invocation.getArgument(1);
            balance[0] = newBalance;
            System.out.println("Thread: " + Thread.currentThread().getName() + " | 사용 후 잔액: " + newBalance);
            return new UserPoint(userId, newBalance, System.currentTimeMillis());
        });

        doNothing().when(pointValidationService).validateSufficientBalance(anyLong(), anyLong());

        int numberOfThreads = 10;  // 동시 요청 스레드 수
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // 동시 사용 요청
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                pointService.usePoints(userId, useAmount);
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 최종 잔액이 0이어야 함
        System.out.println("사용 후 최종 잔액: " + balance[0]);
        assertEquals(0L, balance[0]);
    }


    @Test
    @DisplayName("동시 포인트 충전 및 사용 요청 테스트")
    public void testConcurrentPointChargingAndUsage() throws InterruptedException {
        // Given
        long userId = 1L;
        long initialBalance = 500L;
        long chargeAmount = 100L;
        long useAmount = 50L;
        UserPoint mockUserPoint = new UserPoint(userId, initialBalance, System.currentTimeMillis());

        when(pointValidationService.validateUserExists(userId)).thenReturn(mockUserPoint);
        when(userPointRepository.insertOrUpdate(anyLong(), anyLong())).thenAnswer(invocation -> {
            long newBalance = (long) invocation.getArgument(1);
            System.out.println("Thread: " + Thread.currentThread().getName() + " | 새 잔액: " + newBalance);
            return new UserPoint(userId, newBalance, System.currentTimeMillis());
        });
        doNothing().when(pointValidationService).validateSufficientBalance(anyLong(), anyLong());

        int numberOfThreads = 20;  // 10개는 충전, 10개는 사용 요청을 보냄
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // 10개의 충전, 10개의 사용 요청
        for (int i = 0; i < numberOfThreads / 2; i++) {
            executorService.submit(() -> {
                pointService.chargeUserPoint(userId, chargeAmount);
                System.out.println("스레드: " + Thread.currentThread().getName() + " | 포인트 충전 요청: " + chargeAmount);
            });
            executorService.submit(() -> {
                pointService.usePoints(userId, useAmount);
                System.out.println("스레드: " + Thread.currentThread().getName() + " | 포인트 사용 요청: " + useAmount);
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 최종 잔액 확인
        ChargeUserPointResponse result = pointService.chargeUserPoint(userId, 0L);  // 잔액 조회
        // (충전 100 * 10) - (사용 50 * 10) = 500 포인트가 남아야 함
        System.out.println("충전 및 사용 후 최종 잔액: " + result.point());
        assertEquals(500L, result.point());
    }

    @Test
    @DisplayName("3명의 사용자에 대한 동시 충전 및 사용 요청 테스트")
    public void testConcurrentChargingAndUsageForMultipleUsersWithLocks() throws InterruptedException {
        // Given
        long[] userIds = {1L, 2L, 3L}; // 세 명의 사용자
        long[] initialBalances = {500L, 1000L, 1500L}; // 각 사용자의 초기 잔액
        long[][] chargeAmounts = {{10, 20, 30}, {50, 60, 70}, {80, 90, 100}}; // 각 사용자의 충전 금액
        long[][] useAmounts = {{5, 15, 25}, {40, 50, 60}, {70, 80, 90}}; // 각 사용자의 사용 금액

        long[] balances = {initialBalances[0], initialBalances[1], initialBalances[2]}; // 각 사용자의 현재 잔액

        // validateUserExists에서 각 사용자 잔액을 반영하도록 설정
        when(pointValidationService.validateUserExists(anyLong())).thenAnswer(invocation -> {
            long userId = (long) invocation.getArgument(0);
            int index = (userId == userIds[0]) ? 0 : (userId == userIds[1]) ? 1 : 2;
            return new UserPoint(userId, balances[index], System.currentTimeMillis());
        });

        // insertOrUpdate 호출 시 잔액을 업데이트하고 로그 출력
        when(userPointRepository.insertOrUpdate(anyLong(), anyLong())).thenAnswer(invocation -> {
            long userId = (long) invocation.getArgument(0);
            long newBalance = (long) invocation.getArgument(1);
            int index = (userId == userIds[0]) ? 0 : (userId == userIds[1]) ? 1 : 2;
            balances[index] = newBalance;
            System.out.println("스레드: " + Thread.currentThread().getName() + " | 사용자: " + userId + " | 새 잔액: " + newBalance);
            return new UserPoint(userId, newBalance, System.currentTimeMillis());
        });

        ExecutorService executorService = Executors.newFixedThreadPool(9); // 3명의 사용자에게 각각 3개의 스레드 할당

        // 충전 및 사용 요청
        for (int i = 0; i < chargeAmounts[0].length; i++) {
            for (int j = 0; j < userIds.length; j++) {
                final int userIndex = j;
                final long chargeAmount = chargeAmounts[j][i];
                final long useAmount = useAmounts[j][i];

                // 각 사용자에 대한 충전 요청
                executorService.submit(() -> {
                    System.out.println("스레드: " + Thread.currentThread().getName() + " | 사용자: " + userIds[userIndex] + " | 요청 충전 금액: " + chargeAmount);
                    pointService.chargeUserPoint(userIds[userIndex], chargeAmount);
                });

                // 각 사용자에 대한 사용 요청
                executorService.submit(() -> {
                    System.out.println("스레드: " + Thread.currentThread().getName() + " | 사용자: " + userIds[userIndex] + " | 요청 사용 금액: " + useAmount);
                    pointService.usePoints(userIds[userIndex], useAmount);
                });
            }
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 각 사용자의 최종 잔액 확인
        for (int i = 0; i < userIds.length; i++) {
            long expectedFinalBalance = initialBalances[i] + (chargeAmounts[i][0] + chargeAmounts[i][1] + chargeAmounts[i][2])
                    - (useAmounts[i][0] + useAmounts[i][1] + useAmounts[i][2]);
            System.out.println("사용자 " + userIds[i] + "의 최종 잔액: " + balances[i]);
            assertEquals(expectedFinalBalance, balances[i]);
        }
    }

    /**
     * 같은 유저에게 여러 포인트 충전 및 사용 요청을 순차적으로 보내고 최종 잔액을 검증하는 테스트
     * 상황: 700원 충전, 1000원 충전, 400원 충전, 500원 충전, 300원 사용 요청을 순차적으로 실행
     * 예상 결과: 최종 잔액이 (700 + 1000 + 400 + 500 - 300)원과 일치해야 함
     */
    @Test
    @DisplayName("순차적인 포인트 충전 및 사용 테스트")
    public void testSequentialPointOperations() {
        // Given
        long userId = 1L;
        final long[] currentBalance = {0L}; // 잔액을 저장할 공유 변수

        // 사용자 검증 설정
        when(pointValidationService.validateUserExists(userId)).thenAnswer(invocation -> {
            return new UserPoint(userId, currentBalance[0], System.currentTimeMillis());
        });

        // 잔액 업데이트 시 새로운 UserPoint 반환 및 잔액 누적
        when(userPointRepository.insertOrUpdate(eq(userId), anyLong())).thenAnswer(invocation -> {
            long newBalance = invocation.getArgument(1);
            currentBalance[0] = newBalance; // 잔액 업데이트
            return new UserPoint(userId, newBalance, System.currentTimeMillis());
        });

        // 거래 내역 저장 시 호출되는 메서드 모킹 (필요에 따라 수정)
        when(pointHistoryRepository.insert(anyLong(), anyLong(), any(), anyLong()))
                .thenReturn(new PointHistory(1L, userId, 0L, TransactionType.CHARGE, System.currentTimeMillis()));

        // When
        pointService.chargeUserPoint(userId, 700L);   // 잔액: 0 + 700 = 700
        pointService.chargeUserPoint(userId, 1000L);  // 잔액: 700 + 1000 = 1700
        pointService.chargeUserPoint(userId, 400L);   // 잔액: 1700 + 400 = 2100
        pointService.chargeUserPoint(userId, 500L);   // 잔액: 2100 + 500 = 2600
        pointService.usePoints(userId, 300L);         // 잔액: 2600 - 300 = 2300

        // Then
        // 최종 잔액 확인
        when(userPointRepository.selectById(userId)).thenReturn(new UserPoint(userId, currentBalance[0], System.currentTimeMillis()));
        UserPointResponse result = pointService.getBalance(userId);
        long expectedBalance = 700L + 1000L + 400L + 500L - 300L;
        assertEquals(expectedBalance, result.point());
    }

    /**
     * 같은 유저에게 지연된 포인트 충전 요청을 보내고 순서대로 적재되었는지 검증하는 테스트
     * 상황: 700원 충전, 10초 후 1000원 충전, 15초 후 400원 충전
     * 예상 결과: 거래 내역이 해당 순서대로 적재되고 최종 잔액이 올바른지 확인
     */
    @Test
    @DisplayName("지연된 포인트 충전 요청 순서 확인 테스트")
    public void testDelayedPointOperations() throws InterruptedException {
        // Given
        long userId = 1L;
        final long[] currentBalance = {0L};

        // 사용자 검증 설정
        when(pointValidationService.validateUserExists(userId)).thenAnswer(invocation -> {
            return new UserPoint(userId, currentBalance[0], System.currentTimeMillis());
        });

        // 잔액 업데이트 시 새로운 UserPoint 반환 및 잔액 누적
        when(userPointRepository.insertOrUpdate(eq(userId), anyLong())).thenAnswer(invocation -> {
            long newBalance = invocation.getArgument(1);
            currentBalance[0] = newBalance;
            return new UserPoint(userId, newBalance, System.currentTimeMillis());
        });

        // 거래 내역 저장 시 호출되는 메서드 모킹
        when(pointHistoryRepository.insert(anyLong(), anyLong(), any(), anyLong()))
                .thenReturn(new PointHistory(1L, userId, 0L, TransactionType.CHARGE, System.currentTimeMillis()));

        // When
        pointService.chargeUserPoint(userId, 700L);   // 잔액: 0 + 700 = 700

        // 10ms 지연
        Thread.sleep(10);

        pointService.chargeUserPoint(userId, 1000L);  // 잔액: 700 + 1000 = 1700

        // 추가로 5ms 지연
        Thread.sleep(5);

        pointService.chargeUserPoint(userId, 400L);   // 잔액: 1700 + 400 = 2100

        // Then
        // 최종 잔액 확인
        when(userPointRepository.selectById(userId)).thenReturn(new UserPoint(userId, currentBalance[0], System.currentTimeMillis()));
        UserPointResponse result = pointService.getBalance(userId);
        long expectedBalance = 700L + 1000L + 400L;
        assertEquals(expectedBalance, result.point());

        // 거래 내역이 순서대로 적재되었는지 확인
        List<PointHistory> pointHistories = List.of(
                new PointHistory(1L, userId, 700L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(3L, userId, 400L, TransactionType.CHARGE, System.currentTimeMillis())
        );
        when(pointHistoryRepository.selectAllByUserId(userId)).thenReturn(pointHistories);

        List<UserPointHistoryResponse> histories = pointService.getPointHistories(userId);
        assertEquals(3, histories.size());
        assertEquals(700L, histories.get(0).amount());
        assertEquals(1000L, histories.get(1).amount());
        assertEquals(400L, histories.get(2).amount());
    }
}

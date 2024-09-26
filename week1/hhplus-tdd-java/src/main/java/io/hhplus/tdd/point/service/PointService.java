package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.dto.response.ChargeUserPointResponse;
import io.hhplus.tdd.point.dto.response.UseUserPointResponse;
import io.hhplus.tdd.point.dto.response.UserPointHistoryResponse;
import io.hhplus.tdd.point.dto.response.UserPointResponse;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@AllArgsConstructor
public class PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointValidationService pointValidationService;
    private final long MAX_POINT_LIMIT = 1_000_000L;
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    /// 사용자 포인트 잔액 조회
    public UserPointResponse getBalance(Long userId) {
        UserPoint userPoint = pointValidationService.validateUserExists(userId);
        return new UserPointResponse(userId, userPoint.point(), userPoint.updateMillis());
    }

    /// 사용자 포인트 사용/충전 내역 조회
    public List<UserPointHistoryResponse> getPointHistories(Long userId) {
        pointValidationService.validateUserExists(userId);
        List<PointHistory> pointHistories = pointHistoryRepository.selectAllByUserId(userId);
        return pointHistories.stream()
                .map(UserPointHistoryResponse::from)
                .toList();
    }

    /// 사용자 포인트 충전
    public ChargeUserPointResponse chargeUserPoint(Long userId, Long amount) {
        return executeWithLock(userId, () -> {
            // 1. 금액 검증
            pointValidationService.checkAmount(amount);

            // 2. 사용자 ID 검증
            UserPoint userPoint = pointValidationService.validateUserExists(userId);

            // 3. 포인트 한도 초과 검증
            long newBalance = userPoint.point() + amount;
            pointValidationService.validateMaxPointLimit(newBalance, MAX_POINT_LIMIT);

            // 포인트 업데이트 및 내역 저장
            UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(userId, newBalance);
            pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return ChargeUserPointResponse.from(updatedUserPoint);
        });
    }

    /// 사용자 포인트 사용
    public UseUserPointResponse usePoints(Long userId, long amount) {
        return executeWithLock(userId, () -> {
            // 1. 금액 검증
            pointValidationService.checkAmount(amount);

            // 2. 사용자 ID 검증
            UserPoint userPoint = pointValidationService.validateUserExists(userId);

            // 3. 잔액 검증
            pointValidationService.validateSufficientBalance(userPoint.point(), amount);

            // 4. 포인트 사용 및 잔액 업데이트
            long newBalance = userPoint.point() - amount;
            UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(userId, newBalance);

            // 포인트 사용 내역 저장
            pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

            return UseUserPointResponse.from(updatedUserPoint);
        });
    }

    // 사용자별 락을 통해 안전하게 작업을 실행하는 메서드
    private <T> T executeWithLock(Long userId, LockedOperation<T> operation) {
        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        try {
            log.info("스레드: {} | 사용자: {} | 락 획득 시도", Thread.currentThread().getName(), userId);
            lock.lock();
            log.info("스레드: {} | 사용자: {} | 락 획득 성공", Thread.currentThread().getName(), userId);
            return operation.execute();
        } finally {
            log.info("스레드: {} | 사용자: {} | 락 해제", Thread.currentThread().getName(), userId);
            lock.unlock();
            cleanupLock(userId, lock);
        }
    }


    // 불필요한 락 제거
    private void cleanupLock(Long userId, ReentrantLock lock) {
        if (lock != null && !lock.isLocked()) {
            userLocks.remove(userId, lock);
        }
    }

    @FunctionalInterface
    private interface LockedOperation<T> {
        T execute();
    }
}


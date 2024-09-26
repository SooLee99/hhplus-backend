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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

// TODO: 타임아웃 설정, MAX_POINT_LIMIT, 싱글(jdk 동시성)/동시 인스턴스 일 때 동시성 처리
@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointValidationService pointValidationService;
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    private final long MAX_POINT_LIMIT = 1_000_000L;

    /**
     * 사용자 포인트 잔액 조회
     * @param userId 사용자 ID
     * @return 사용자 포인트 잔액
     */
    public UserPointResponse getBalance(Long userId) {
        log.info("사용자 포인트 잔액 조회: userId={}", userId);
        UserPoint userPoint = pointValidationService.validateUserExists(userId);
        return new UserPointResponse(userId, userPoint.point(), userPoint.updateMillis());
    }

    /**
     * 사용자 포인트 사용/충전 내역 조회
     * @param userId 사용자 ID
     * @return 포인트 거래 내역 리스트
     */
    public List<UserPointHistoryResponse> getPointHistories(Long userId) {
        log.info("사용자 포인트 사용/충전 내역 조회: userId={}", userId);
        pointValidationService.validateUserExists(userId);
        List<PointHistory> pointHistories = pointHistoryRepository.selectAllByUserId(userId);
        return pointHistories.stream()
                .map(UserPointHistoryResponse::from)
                .toList();
    }

    /**
     * 사용자 포인트 충전
     * @param userId 사용자 ID
     * @param amount 충전 금액
     * @return 충전 결과
     */
    public ChargeUserPointResponse chargeUserPoint(Long userId, Long amount) {
        return executeWithLock(userId, () -> {
            log.info("사용자 포인트 충전 요청: userId={}, 충전 금액={}", userId, amount);

            // 금액 검증
            pointValidationService.checkAmount(amount);

            // 사용자 존재 여부 검증
            UserPoint userPoint = pointValidationService.validateUserExists(userId);

                long newBalance = userPoint.point() + amount;

            // 포인트 업데이트 및 내역 저장
            UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(userId, newBalance);
            pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

            log.info("사용자 포인트 충전 완료: userId={}, 잔액={}", userId, newBalance);
            return ChargeUserPointResponse.from(updatedUserPoint);
        });
    }

    /**
     * 사용자 포인트 사용
     * @param userId 사용자 ID
     * @param amount 사용 금액
     * @return 포인트 사용 결과
     */
    public UseUserPointResponse usePoints(Long userId, long amount) {
        return executeWithLock(userId, () -> {
            log.info("사용자 포인트 사용 요청: userId={}, 사용 금액={}", userId, amount);

            // 금액 검증
            pointValidationService.checkAmount(amount);

            // 사용자 존재 여부 검증
            UserPoint userPoint = pointValidationService.validateUserExists(userId);

            // 잔액 부족 검증
            pointValidationService.validateSufficientBalance(userPoint.point(), amount);

            // 포인트 사용 및 잔액 업데이트
            long newBalance = userPoint.point() - amount;
            UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(userId, newBalance);

            // 포인트 사용 내역 저장
            pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

            log.info("사용자 포인트 사용 완료: userId={}, 잔액={}", userId, newBalance);
            return UseUserPointResponse.from(updatedUserPoint.id(), updatedUserPoint.point(), updatedUserPoint.updateMillis());        });
    }

    /**
     * 사용자별 락을 통해 안전하게 작업을 실행하는 메서드
     * @param userId 사용자 ID
     * @param operation 실행할 작업
     * @param <T> 작업의 반환 타입
     * @return 작업의 결과
     */
    private <T> T executeWithLock(Long userId, LockedOperation<T> operation) {
        ReentrantLock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
        boolean acquired = false;
        try {
            log.info("스레드: {} | 사용자: {} | 락 획득 시도", Thread.currentThread().getName(), userId);

            // 락 획득 시도 (타임아웃 설정)
            acquired = lock.tryLock(10, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("스레드: {} | 사용자: {} | 락 획득 실패: 타임아웃 초과", Thread.currentThread().getName(), userId);
                throw new RuntimeException("요청 처리 중 타임아웃이 발생했습니다."); // 적절한 커스텀 예외로 변경 가능
            }

            log.info("스레드: {} | 사용자: {} | 락 획득 성공", Thread.currentThread().getName(), userId);
            return operation.execute();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("스레드 인터럽트 발생: {}", e.getMessage());
            throw new RuntimeException("요청 처리 중 오류가 발생했습니다.", e); // 적절한 커스텀 예외로 변경 가능
        } finally {
            if (acquired) {
                lock.unlock();
                log.info("스레드: {} | 사용자: {} | 락 해제", Thread.currentThread().getName(), userId);
                cleanupLock(userId, lock);
            }
        }
    }

    /**
     * 불필요한 락 제거
     * @param userId 사용자 ID
     * @param lock 락 객체
     */
    private void cleanupLock(Long userId, ReentrantLock lock) {
        if (lock != null && !lock.isLocked()) {
            log.info("사용자: {} | 불필요한 락 제거", userId);
            userLocks.remove(userId, lock);
        }
    }

    @FunctionalInterface
    private interface LockedOperation<T> {
        T execute();
    }
}

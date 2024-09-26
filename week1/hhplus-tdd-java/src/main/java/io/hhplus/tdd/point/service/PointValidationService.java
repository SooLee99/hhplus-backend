package io.hhplus.tdd.point.service;

import io.hhplus.tdd.exception.InsufficientBalanceException;
import io.hhplus.tdd.exception.InvalidAmountException;
import io.hhplus.tdd.exception.MaxPointLimitExceededException;
import io.hhplus.tdd.exception.UserNotFoundException;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PointValidationService {

    private final UserPointRepository userPointRepository;

    // 금액 검증
    public void checkAmount(Long amount) {
        if (amount == null || amount <= 0) {
            log.error("유효하지 않은 금액입니다. amount={}", amount);
            throw new InvalidAmountException();
        }
    }

    // 사용자 존재 여부 검증
    public UserPoint validateUserExists(Long userId) {
        UserPoint userPoint = userPointRepository.selectById(userId);
        if (userPoint == null) {
            log.error("존재하지 않는 사용자 ID입니다. userId={}", userId);
            throw new UserNotFoundException();
        }
        return userPoint;
    }

    // 포인트 한도 초과 검증
    public void validateMaxPointLimit(long newBalance, long maxPointLimit) {
        if (newBalance > maxPointLimit) {
            log.error("포인트 한도 초과: newBalance={}, maxPointLimit={}", newBalance, maxPointLimit);
            throw new MaxPointLimitExceededException();
        }
    }

    // 잔액 부족 검증
    public void validateSufficientBalance(long currentBalance, long amount) {
        if (currentBalance < amount) {
            log.error("잔액 부족: currentBalance={}, 사용하려는 금액={}", currentBalance, amount);
            throw new InsufficientBalanceException();
        }
    }
}

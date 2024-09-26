package io.hhplus.tdd.point.service;

import io.hhplus.tdd.exception.*;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointValidationService {

    private final UserPointRepository userPointRepository;

    /**
     * 금액 검증
     * @param amount 충전/사용하려는 금액
     * @throws InvalidAmountException 유효하지 않은 금액일 경우 예외 발생
     */
    public void checkAmount(Long amount) {
        Assert.notNull(amount, ErrorCode.INVALID_AMOUNT.getMessage());
        if (amount <= 0) {
            log.error("유효하지 않은 금액: {}", amount);
            throw new InvalidAmountException();
        }
    }

    /**
     * 사용자 존재 여부 검증
     * @param userId 사용자 ID
     * @return UserPoint 사용자 포인트 정보
     * @throws UserNotFoundException 존재하지 않는 사용자일 경우 예외 발생
     */
    public UserPoint validateUserExists(Long userId) {
        Objects.requireNonNull(userId, ErrorCode.NULL_USER_ID.getMessage());

        if (userId <= 0) {
            log.error("유효하지 않은 사용자 ID: {}", userId);
            throw new InvalidUserIdException();
        }

        UserPoint userPoint = userPointRepository.selectById(userId);
        if (userPoint == null) {
            log.error("존재하지 않는 사용자: userId={}", userId);
            throw new UserNotFoundException();
        }
        return userPoint;
    }

    /**
     * 잔액 부족 검증
     * @param currentBalance 현재 사용자 잔액
     * @param amount 사용하려는 금액
     * @throws InsufficientBalanceException 잔액이 부족할 경우 예외 발생
     */
    public void validateSufficientBalance(long currentBalance, long amount) {
        if (currentBalance < amount) {
            log.error("잔액 부족: 현재 잔액={}, 사용하려는 금액={}", currentBalance, amount);
            throw new InsufficientBalanceException();
        }

        if (currentBalance < 0) {
            log.error("유효하지 않은 잔액: 잔액이 음수임, currentBalance={}", currentBalance);
            throw new InsufficientBalanceException();
        }
    }
}

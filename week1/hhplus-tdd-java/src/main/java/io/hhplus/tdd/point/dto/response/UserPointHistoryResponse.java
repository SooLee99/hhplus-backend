package io.hhplus.tdd.point.dto.response;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;

// record 정의: 필드들을 한 줄로 선언해야 함
public record UserPointHistoryResponse(
        long id,            // 거래 내역 ID
        long userId,         // 사용자 ID
        long amount,         // 포인트 금액
        TransactionType type, // 트랜잭션 유형 (충전 or 사용)
        long updateMillis    // 업데이트 시간 (밀리초)
) {
    public static UserPointHistoryResponse from(PointHistory pointHistory) {
        return new UserPointHistoryResponse(
                pointHistory.id(),
                pointHistory.userId(),
                pointHistory.amount(),
                pointHistory.type(),
                pointHistory.updateMillis()
        );
    }
}

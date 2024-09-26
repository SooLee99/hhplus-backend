package io.hhplus.tdd.point.dto.response;

import io.hhplus.tdd.point.domain.UserPoint;
import java.io.Serializable;

public record UseUserPointResponse(
        long id,
        long updatedPoint,
        String message
) implements Serializable {
    public static UseUserPointResponse from(UserPoint userPoint) {
        return new UseUserPointResponse(
                userPoint.id(),
                userPoint.point(),
                "포인트 사용 완료"
        );
    }
}

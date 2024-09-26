package io.hhplus.tdd.point.dto.response;

import io.hhplus.tdd.point.domain.UserPoint;
import java.io.Serializable;

public record ChargeUserPointResponse(
        long id,
        long point,
        long updateMillis
) implements Serializable {
    public static ChargeUserPointResponse from(UserPoint userPoint){
        return new ChargeUserPointResponse(userPoint.id(), userPoint.point(), userPoint.updateMillis());
    }
}
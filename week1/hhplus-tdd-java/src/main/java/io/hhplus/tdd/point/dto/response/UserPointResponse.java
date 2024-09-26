package io.hhplus.tdd.point.dto.response;
import io.hhplus.tdd.point.domain.UserPoint;
import java.io.Serializable;

public record UserPointResponse(
        long id,
        long point,
        long updateMillis
) implements Serializable {
    public static UserPointResponse from(UserPoint userPoint){
        return new UserPointResponse(userPoint.id(), userPoint.point(), userPoint.updateMillis());
    }
}
package io.hhplus.tdd.point.dto.response;

import io.hhplus.tdd.point.domain.UserPoint;

import java.io.Serializable;

public record UseUserPointResponse(
        long id,
        long point,
        long updateMillis
) implements Serializable {
    public static UseUserPointResponse from(long id, long point, long updateMillis){
        return new UseUserPointResponse(id, point, updateMillis);
    }

}
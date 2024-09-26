package io.hhplus.tdd.exception;

public record ErrorResponse(
        String code,
        String message
) {
}

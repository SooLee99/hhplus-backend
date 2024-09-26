package io.hhplus.tdd.point.controller;
import io.hhplus.tdd.exception.InsufficientBalanceException;
import io.hhplus.tdd.exception.InvalidAmountException;
import io.hhplus.tdd.exception.UserNotFoundException;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.dto.response.ChargeUserPointResponse;
import io.hhplus.tdd.point.dto.response.UseUserPointResponse;
import io.hhplus.tdd.point.dto.response.UserPointHistoryResponse;
import io.hhplus.tdd.point.dto.response.UserPointResponse;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PointController 테스트 클래스
 */
@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    // ------------------------------ 2.1. 포인트 충전 API 테스트 ------------------------------ //

    /**
     * 2.1.1. 정상적인 포인트 충전 요청 테스트
     * 상황: 사용자가 포인트를 정상적으로 충전하는 요청을 보낼 때.
     * 입력: 유효한 사용자 ID 및 금액.
     * 예상 결과: 200 OK 응답 및 "포인트 충전 완료" 메시지 반환.
     */
    @Test
    @DisplayName("정상적인 포인트 충전 요청")
    public void testChargePoints_Success() throws Exception {
        // Given
        long userId = 1L;
        ChargeUserPointResponse mockResponse = new ChargeUserPointResponse(userId, 1500L, System.currentTimeMillis());

        // Mock 설정
        when(pointService.chargeUserPoint(anyLong(), anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(1500L));
    }

    /**
     * 2.1.2. 존재하지 않는 사용자 ID로 충전 요청 테스트
     * 상황: 존재하지 않는 사용자 ID로 충전 요청.
     * 예상 결과: 404 Not Found 응답 및 에러 메시지 반환.
     */
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 충전 요청")
    public void testChargePoints_InvalidUser() throws Exception {
        // Given
        long invalidUserId = 999L;
        doThrow(new UserNotFoundException("존재하지 않는 사용자 ID입니다."))
                .when(pointService).chargeUserPoint(eq(invalidUserId), anyLong());

        // When & Then
        mockMvc.perform(patch("/point/{id}/charge", invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":500}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    /**
     * 2.1.3. 잘못된 금액으로 충전 요청 테스트
     * 상황: 충전 금액이 0이거나 음수일 경우.
     * 예상 결과: 400 Bad Request 응답 및 에러 메시지 반환.
     */
    @Test
    @DisplayName("잘못된 금액으로 충전 요청")
    public void testChargePoints_InvalidAmount() throws Exception {
        // Given
        long userId = 1L;
        long invalidAmount = 0L;
        doThrow(new IllegalArgumentException("유효하지 않은 금액입니다."))
                .when(pointService).chargeUserPoint(eq(userId), eq(invalidAmount));

        // When & Then
        mockMvc.perform(patch("/point/{id}/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------ 2.2. 포인트 사용 API 테스트 ------------------------------ //

    /**
     * 2.2.1. 정상적인 포인트 사용 요청 테스트
     * 상황: 사용자가 잔액 범위 내에서 포인트 사용 요청.
     * 예상 결과: 200 OK 응답 및 "포인트 사용 완료" 메시지 반환.
     */
    @Test
    @DisplayName("정상적인 포인트 사용 요청")
    public void testUsePoints_Success() throws Exception {
        // Given
        long userId = 1L;
        long useAmount = 200L;
        UseUserPointResponse mockResponse = new UseUserPointResponse(userId, 800L, "포인트 사용 완료");

        // Mock 설정
        when(pointService.usePoints(anyLong(), anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":200}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.updatedPoint").value(800L))
                .andExpect(jsonPath("$.message").value("포인트 사용 완료"));
    }

    /**
     * 2.2.2. 잔액 부족 시 포인트 사용 요청 테스트
     * 상황: 사용자가 잔액보다 더 많은 포인트를 사용하려는 요청.
     * 예상 결과: 400 Bad Request 응답 및 에러 메시지 반환.
     */
    @Test
    @DisplayName("잔액 부족 시 포인트 사용 요청")
    public void testUsePoints_InsufficientBalance() throws Exception {
        // Given
        long userId = 1L;
        long useAmount = 2000L;
        doThrow(new InsufficientBalanceException("잔액이 부족합니다."))
                .when(pointService).usePoints(eq(userId), eq(useAmount));

        // When & Then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":2000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
    }

    /**
     * 2.2.3. 잘못된 금액으로 포인트 사용 요청 테스트
     * 상황: 사용자가 0 이하의 금액으로 포인트 사용 요청.
     * 예상 결과: 400 Bad Request 응답 및 에러 메시지 반환.
     */
    @Test
    @DisplayName("잘못된 금액으로 포인트 사용 요청")
    public void testUsePoints_InvalidAmount() throws Exception {
        // Given
        long userId = 1L;
        long invalidAmount = -100L;

        // Mock 설정: 유효하지 않은 금액일 때 예외 발생
        doThrow(new InvalidAmountException("유효하지 않은 금액입니다."))
                .when(pointService).usePoints(eq(userId), eq(invalidAmount));

        // When & Then
        mockMvc.perform(patch("/point/{id}/use", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 금액입니다."));
    }

    // ------------------------------ 2.3. 포인트 잔액 조회 API 테스트 ------------------------------ //

    /**
     * 2.3.1. 정상적인 잔액 조회 요청 테스트
     * 상황: 사용자가 자신의 잔액을 조회하는 요청.
     * 예상 결과: 200 OK 응답 및 잔액이 반환됨.
     */
    @Test
    @DisplayName("정상적인 잔액 조회 요청")
    public void testGetBalance_Success() throws Exception {
        // Given
        long userId = 1L;
        UserPointResponse mockResponse = new UserPointResponse(userId, 1000L, System.currentTimeMillis());

        // Mock 설정
        when(pointService.getBalance(anyLong())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.point").value(1000L));
    }

    /**
     * 2.3.2. 존재하지 않는 사용자 ID로 잔액 조회 요청 테스트
     * 상황: 존재하지 않는 사용자 ID로 잔액 조회 요청.
     * 예상 결과: 404 Not Found 응답 및 에러 메시지 반환.
     */
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 잔액 조회 요청")
    public void testGetBalance_InvalidUser() throws Exception {
        // Given
        long invalidUserId = 999L;
        doThrow(new UserNotFoundException("존재하지 않는 사용자 ID입니다."))
                .when(pointService).getBalance(eq(invalidUserId));

        // When & Then
        mockMvc.perform(get("/point/{id}", invalidUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }

    // ------------------------------ 2.4. 포인트 거래 내역 조회 API 테스트 ------------------------------ //

    /**
     * 2.4.1. 정상적인 거래 내역 조회 요청 테스트
     * 상황: 사용자가 자신의 거래 내역을 조회하는 요청.
     * 예상 결과: 200 OK 응답 및 거래 내역이 반환됨.
     */
    @Test
    @DisplayName("정상적인 거래 내역 조회 요청")
    public void testGetPointHistories_Success() throws Exception {
        // Given
        long userId = 1L;
        List<UserPointHistoryResponse> mockHistories = List.of(
                new UserPointHistoryResponse(1L, userId, 500L, TransactionType.CHARGE, System.currentTimeMillis())
        );

        // Mock 설정
        when(pointService.getPointHistories(anyLong())).thenReturn(mockHistories);

        // When & Then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(500L));
    }

    /**
     * 2.4.2. 거래 내역이 없는 경우 테스트
     * 상황: 사용자의 거래 내역이 없는 경우.
     * 예상 결과: 200 OK 응답 및 빈 리스트 반환.
     */
    @Test
    @DisplayName("거래 내역이 없는 경우")
    public void testGetPointHistories_Empty() throws Exception {
        // Given
        long userId = 1L;
        when(pointService.getPointHistories(anyLong())).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/point/{id}/histories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * 2.4.3. 존재하지 않는 사용자 ID로 거래 내역 조회 요청 테스트
     * 상황: 존재하지 않는 사용자 ID로 거래 내역 조회 요청.
     * 예상 결과: 404 Not Found 응답 및 에러 메시지 반환.
     */
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 거래 내역 조회 요청")
    public void testGetPointHistories_InvalidUser() throws Exception {
        // Given
        long invalidUserId = 999L;
        doThrow(new UserNotFoundException("존재하지 않는 사용자 ID입니다."))
                .when(pointService).getPointHistories(eq(invalidUserId));

        // When & Then
        mockMvc.perform(get("/point/{id}/histories", invalidUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."));
    }
}

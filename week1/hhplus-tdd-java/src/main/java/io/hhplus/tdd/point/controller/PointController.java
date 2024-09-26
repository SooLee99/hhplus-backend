package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.point.dto.request.ChargePointRequest;
import io.hhplus.tdd.point.dto.request.UsePointRequest;
import io.hhplus.tdd.point.dto.response.ChargeUserPointResponse;
import io.hhplus.tdd.point.dto.response.UseUserPointResponse;
import io.hhplus.tdd.point.dto.response.UserPointHistoryResponse;
import io.hhplus.tdd.point.dto.response.UserPointResponse;
import io.hhplus.tdd.point.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {
    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * 사용자 포인트 잔액 조회
     */
    @GetMapping("{id}")
    public ResponseEntity<UserPointResponse> point(@PathVariable long id) {
        UserPointResponse response = pointService.getBalance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 포인트 사용/충전 내역 조회
     */
    @GetMapping("{id}/histories")
    public ResponseEntity<List<UserPointHistoryResponse>> history(@PathVariable long id) {
        List<UserPointHistoryResponse> histories = pointService.getPointHistories(id);
        return ResponseEntity.ok(histories);
    }

    /**
     * 사용자 포인트 충전
     */
    @PatchMapping("{id}/charge")
    public ResponseEntity<ChargeUserPointResponse> charge(@PathVariable long id, @RequestBody ChargePointRequest request) {
        ChargeUserPointResponse response = pointService.chargeUserPoint(id, request.getAmount());
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 포인트 사용
     */
    @PatchMapping("{id}/use")
    public ResponseEntity<UseUserPointResponse> usePoints(@PathVariable long id, @RequestBody UsePointRequest request) {
        UseUserPointResponse response = pointService.usePoints(id, request.getAmount());
        return ResponseEntity.ok(response);
    }
}

package io.hhplus.tdd.point.repository;


import io.hhplus.tdd.point.domain.UserPoint;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointRepository {
    UserPoint selectById(Long userId);
    UserPoint insertOrUpdate(Long userId, long amount);
}
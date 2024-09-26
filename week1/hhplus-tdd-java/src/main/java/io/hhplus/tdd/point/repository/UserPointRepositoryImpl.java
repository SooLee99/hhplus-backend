package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPointRepositoryImpl  implements UserPointRepository {
    private final UserPointTable userPointTable;

    @Override
    public UserPoint selectById(Long userId) {
        return userPointTable.selectById(userId);
    }

    @Override
    public UserPoint insertOrUpdate(Long userId, long amount) {
        return userPointTable.insertOrUpdate(userId, amount);
    }
}

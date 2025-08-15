package io.hhplus.tdd.point.application;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

  private final UserPointTable userPoint;
  private final PointHistoryTable pointHistory;

  public UserPoint getUserPoints(long userId) {
    return this.userPoint.selectById(userId);
  }

  public List<PointHistory> getUserPointHistories(long userId) {
    return this.pointHistory.selectAllByUserId(userId);
  }
}

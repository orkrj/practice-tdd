package io.hhplus.tdd.point.application;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
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

  public UserPoint chargePoints(long userId, long amount) {
    long checkedAmount = this.checkChargeAmount(amount);

    try{
      long currentPoints = this.userPoint.selectById(userId).point();
      UserPoint chargedUserPoints = this.userPoint.insertOrUpdate(userId, currentPoints + checkedAmount);
      this.pointHistory.insert(userId, checkedAmount, TransactionType.CHARGE, System.currentTimeMillis());
      return chargedUserPoints;

    } catch (Exception e){
      throw new RuntimeException("포인트 충전 중 오류가 발생했습니다.", e);
    }
  }

  // -------------------
  private long checkChargeAmount(long amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
    }

    return amount;
  }
}

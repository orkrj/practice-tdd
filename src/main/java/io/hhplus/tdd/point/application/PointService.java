package io.hhplus.tdd.point.application;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

  private final UserPointTable userPoint;
  private final PointHistoryTable pointHistory;

  private final ConcurrentHashMap<Long, ReentrantLock> userLock = new ConcurrentHashMap<>();

  public UserPoint getUserPoints(long userId) {
    return this.userPoint.selectById(userId);
  }

  public List<PointHistory> getUserPointHistories(long userId) {
    return this.pointHistory.selectAllByUserId(userId);
  }

  public UserPoint chargePoints(long userId, long amount) {
    long positiveAmount = this.checkAmountIsPositive(amount, TransactionType.CHARGE);

    ReentrantLock lock = this.getUserLock(userId);
    lock.lock();

    try{
      long currentPoints = this.userPoint.selectById(userId).point();
      UserPoint chargedUserPoints = this.userPoint.insertOrUpdate(userId, currentPoints + positiveAmount);
      this.pointHistory.insert(userId, positiveAmount, TransactionType.CHARGE, System.currentTimeMillis());
      return chargedUserPoints;

    } catch (Exception e){
      throw new RuntimeException("포인트 충전 중 오류가 발생했습니다.", e);

    } finally{
      lock.unlock();

      if (lock.tryLock()) {
        try {
          if (!lock.hasQueuedThreads()) {
            userLock.remove(userId, lock);
          }
        } finally{
          lock.unlock();
        }
      }
    }
  }

  public UserPoint usePoints(long userId, long amount) {
    long positiveAmount = this.checkAmountIsPositive(amount, TransactionType.USE);

    ReentrantLock lock = this.getUserLock(userId);
    lock.lock();

    try{
      long currentPoints = this.userPoint.selectById(userId).point();
      this.validateSufficientPoints(currentPoints, amount);
      long updatedPoints = currentPoints - positiveAmount;

      UserPoint updatedUserPoints = this.userPoint.insertOrUpdate(userId, updatedPoints);
      this.pointHistory.insert(userId, -amount, TransactionType.USE, System.currentTimeMillis());
      return updatedUserPoints;

    } catch (Exception e){
      if (e instanceof IllegalArgumentException) {
        throw e;
      }

      throw new RuntimeException("포인트 사용 중 오류가 발생했습니다.", e);

    } finally{
      lock.unlock();

      if (lock.tryLock()) {
        try {
          if (!lock.hasQueuedThreads()) {
            userLock.remove(userId, lock);
          }
        } finally{
          lock.unlock();
        }
      }
    }
  }

  // ------------------- private methods -------------------
  private long checkAmountIsPositive(long amount, TransactionType transactionType) {
    String type;
    switch (transactionType) {
      case CHARGE -> type = "충전";
      case USE -> type = "사용";
      default -> throw new IllegalArgumentException("충전 및 사용이 불가능한 유형입니다.");
    }

    if (amount <= 0) {
      throw new IllegalArgumentException(type + " 금액은 0보다 커야 합니다.");
    }

    return amount;
  }

  private void validateSufficientPoints(long currentPoints, long amount) {
    if (currentPoints < amount) {
      throw new IllegalArgumentException("포인트가 부족합니다.");
    }
  }

  private ReentrantLock getUserLock(long userId) {
    return userLock.computeIfAbsent(userId, id -> new ReentrantLock());
  }
}

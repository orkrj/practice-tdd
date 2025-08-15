package io.hhplus.tdd.point.application;

import static org.assertj.core.api.Assertions.*;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PointServiceTest {

  UserPointTable userPointTable;
  PointHistoryTable pointHistoryTable;
  PointService sut;

  @BeforeEach
  void setUp() {
    userPointTable = new UserPointTable();
    pointHistoryTable = new PointHistoryTable();
    sut = new PointService(userPointTable, pointHistoryTable);
  }

  @Nested
  @DisplayName("특정 유저의 포인트를 조회하는 기능")
  class getPoint {

    @Test
    @DisplayName("유저 포인트 테이블에 없는 유저 ID 를 조회하면 새로운 UserPoint 를 반환한다.")
    void givenUserId_whenGetUserPointFirstTime_thenReturnNewUserPoint() {
      // given
      long userId = 1L;

      // when
      UserPoint userPoint = sut.getUserPoints(userId);

      // then
      assertThat(userPoint).isNotNull();
      assertThat(userPoint.point()).isEqualTo(0);
    }

    @Test
    @DisplayName("유저 포인트 테이블에 있는 유저 ID 를 조회하면 기존의 UserPoint 를 반환한다.")
    void givenUserId_whenGetUserPoint_thenReturnUserPoint() {
      // given
      long userId = 1L;
      userPointTable.selectById(userId);
      userPointTable.insertOrUpdate(userId, 100L);

      // when
      UserPoint userPoint = sut.getUserPoints(userId);

      // then
      assertThat(userPoint).isNotNull();
      assertThat(userPoint.point()).isEqualTo(100L);
    }
  }

  @Nested
  @DisplayName("특정 유저의 포인트 충전/이용 내역을 조회하는 기능")
  class getUserPointHistories {

    @Test
    @DisplayName("유저 포인트 충전/이용 내역이 없는 경우 빈 리스트를 반환한다.")
    void givenUserId_whenGetUserPointHistories_thenReturnEmptyList() {
      // given
      long userId = 1L;

      // when
      var histories = sut.getUserPointHistories(userId);

      // then
      assertThat(histories).isEmpty();
    }

    @Test
    @DisplayName("유저 포인트 충전 내역이 있는 경우 해당 내역을 반환한다.")
    void givenUserId_whenGetUserPointHistories_thenReturnChargeHistories() {
      // given
      long userId = 1L;
      pointHistoryTable.insert(userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis());

      // when
      var histories = sut.getUserPointHistories(userId);

      // then
      assertThat(histories).hasSize(1);
      assertThat(histories.get(0).amount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("유저 포인트 이용 내역이 있는 경우 해당 내역을 반환한다.")
    void givenUserId_whenGetUserPointHistories_thenReturnUseHistories() {
      // given
      long userId = 1L;
      pointHistoryTable.insert(userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
      pointHistoryTable.insert(userId, -500L, TransactionType.USE, System.currentTimeMillis());

      // when
      var histories = sut.getUserPointHistories(userId);

      // then
      assertThat(histories).hasSize(2);
      assertThat(histories.get(0).amount()).isEqualTo(1000L);
      assertThat(histories.get(1).amount()).isEqualTo(-500L);
    }
  }

  @Nested
  @DisplayName("특정 유저의 포인트를 충전하는 기능")
  class chargePoints {

    @Test
    @DisplayName("유저 포인트를 충전할 때 충전 금액이 0 이하인 경우 예외가 발생한다.")
    void givenZeroOrNegativeAmount_whenChargePoints_thenThrowRuntimeException() {
      // given
      long userId = 1L;
      long invalidAmount = 0L;

      // when & then
      assertThatThrownBy(() -> sut.chargePoints(userId, invalidAmount)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("정상 금액으로 유저 포인트를 충전하면 기존의 포인트에 충전 금액이 더해진다.")
    void givenValidAmount_whenChargePoints_thenUpdateUserPoint() {
      // given
      long userId = 1L;
      long currentPoints = 1000L;
      long chargeAmounts = 4000L;

      userPointTable.insertOrUpdate(userId, currentPoints);

      // when
      var chargedUserPoints = sut.chargePoints(userId, chargeAmounts);

      // then
      assertThat(chargedUserPoints).isNotNull();
      assertThat(chargedUserPoints.point()).isEqualTo(currentPoints + chargeAmounts);
    }

    @Test
    @DisplayName("유저 포인트를 충전하면 포인트 충전 내역이 기록된다.")
    void givenValidAmount_whenChargePoints_thenRecordPointHistory() {
      // given
      long userId = 1L;
      long firstChargeAmounts = 1000L;
      long secondChargeAmounts = 2000L;

      sut.chargePoints(userId, firstChargeAmounts);
      sut.chargePoints(userId, secondChargeAmounts);

      // when
      var histories = sut.getUserPointHistories(userId);

      // then
      assertThat(histories).hasSize(2);
      assertThat(histories.get(0).amount()).isEqualTo(firstChargeAmounts);
      assertThat(histories.get(1).amount()).isEqualTo(secondChargeAmounts);
    }
  }
}

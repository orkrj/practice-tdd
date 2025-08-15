package io.hhplus.tdd.point.application;

import static org.assertj.core.api.Assertions.*;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import org.junit.jupiter.api.AfterEach;
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

  @AfterEach
  void clean() {
    userPointTable = new UserPointTable();
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
}

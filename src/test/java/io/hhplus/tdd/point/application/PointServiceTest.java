package io.hhplus.tdd.point.application;

import static org.assertj.core.api.Assertions.*;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.domain.UserPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PointServiceTest {

  PointService sut;
  UserPointTable userPointTable;

  @BeforeEach
  void setUp() {
    userPointTable = new UserPointTable();
    sut = new PointService(userPointTable, null);
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
}

package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.zylex.betbot.BetBotApplication.BET_START_TIME;
import static com.zylex.betbot.BetBotApplication.BOT_START_TIME;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Game findByLink(String link);

    @Query("SELECT g FROM Game g WHERE g.dateTime >= :startDateTime AND g.dateTime <= :endDateTime")
    List<Game> findBetweenDateTimes(@Param("startDateTime") LocalDateTime startDateTime,
                                    @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT g FROM Game g WHERE g.dateTime >= :startDateTime")
    List<Game> findSinceDateTime(@Param("startDateTime") LocalDateTime startDateTime);

    default List<Game> findByBetStartTime() {
        return findSinceDateTime(LocalDateTime.of(BOT_START_TIME.toLocalDate().minusDays(1), BET_START_TIME));
    }
}

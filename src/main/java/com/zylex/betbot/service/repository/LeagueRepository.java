package com.zylex.betbot.service.repository;

import com.zylex.betbot.model.game.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    @Query(value = "SELECT league_link FROM exclude_league WHERE rule_name = :ruleName",
            nativeQuery = true)
    List<String> findExcludeLeagues(@Param("ruleName") String ruleName);

    @Query(value = "SELECT league_link FROM selected_league",
            nativeQuery = true)
    List<String> findAllSelectedLeagues();
}

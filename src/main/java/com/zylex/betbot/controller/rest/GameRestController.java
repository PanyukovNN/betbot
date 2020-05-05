package com.zylex.betbot.controller.rest;

import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.service.repository.GameRepository;
import com.zylex.betbot.service.repository.RuleRepository;
import com.zylex.betbot.service.rule.RuleProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("game")
public class GameRestController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd,MM,yyyy");

    private final GameRepository gameRepository;

    private final RuleProcessor ruleProcessor;

    private final RuleRepository ruleRepository;

    @Autowired
    public GameRestController(GameRepository gameRepository,
                              RuleProcessor ruleProcessor,
                              RuleRepository ruleRepository) {
        this.gameRepository = gameRepository;
        this.ruleProcessor = ruleProcessor;
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Game>> getGamesByDate(@RequestParam(name = "date") String dateText) {
        try {
            LocalDate date = LocalDate.parse(dateText, DATE_FORMATTER);
            List<Game> games = gameRepository.findByDate(date);
            games.sort(Comparator.comparing(Game::getDateTime));
            return new ResponseEntity<>(games, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/since")
    public ResponseEntity<List<Game>> getGamesSinceDate(@RequestParam(name = "date") String dateText) {
        try {
            LocalDate date = LocalDate.parse(dateText, DATE_FORMATTER);
            List<Game> games = gameRepository.findSinceDateTime(LocalDateTime.of(date, LocalTime.MIN));
            games.sort(Comparator.comparing(Game::getDateTime));
            return new ResponseEntity<>(games, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/parse")
    public ResponseEntity<List<Game>> getParsedGames() {
        try {
            ruleProcessor.process();
            List<Game> games = gameRepository.findByBetStartTime();
            return new ResponseEntity<>(ruleProcessor.filterGamesByRules(games, ruleRepository.findByActivateTrue()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

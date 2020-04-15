package com.zylex.betbot.controller.rest;

import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.service.bet.BetProcessor;
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

import static com.zylex.betbot.BetBotApplication.betStartTime;
import static com.zylex.betbot.BetBotApplication.botStartTime;

@RestController
@RequestMapping("game")
public class GameRestController {

    private GameRepository gameRepository;

    private RuleProcessor ruleProcessor;

    private RuleRepository ruleRepository;

    private BetProcessor betProcessor;

    @Autowired
    public GameRestController(GameRepository gameRepository,
                              RuleProcessor ruleProcessor,
                              RuleRepository ruleRepository,
                              BetProcessor betProcessor) {
        this.gameRepository = gameRepository;
        this.ruleProcessor = ruleProcessor;
        this.ruleRepository = ruleRepository;
        this.betProcessor = betProcessor;
    }

    @GetMapping
    public ResponseEntity<List<Game>> getGamesByDate(@RequestParam(name = "date") String dateText) {
            try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd,MM,yyyy");
            LocalDate date = LocalDate.parse(dateText, dateFormatter);
            List<Game> games = gameRepository.getByDate(date);
            games.sort(Comparator.comparing(Game::getDateTime));
            return new ResponseEntity<>(games, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Game>> getGamesSinceDate(@RequestParam(name = "since_date") String dateText) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd,MM,yyyy");
            LocalDate date = LocalDate.parse(dateText, dateFormatter);
            List<Game> games = gameRepository.getSinceDateTime(LocalDateTime.of(date, LocalTime.MIN));
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
            List<Game> games = gameRepository.getSinceDateTime(LocalDateTime.of(botStartTime.toLocalDate().minusDays(1), betStartTime));
            return new ResponseEntity<>(ruleProcessor.filterGamesByRules(games, ruleRepository.getActivated()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @GetMapping("/make_bets")
//    public ResponseEntity<List<Game>> processBets() {
//        try {
//            List<Game> betMadeGames = betProcessor.process();
//            return new ResponseEntity<>(betMadeGames, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
}

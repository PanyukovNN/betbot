package com.zylex.betbot.service.rest;

import com.zylex.betbot.model.game.Game;
import com.zylex.betbot.model.rule.Rule;
import com.zylex.betbot.service.bet.BetProcessor;
import com.zylex.betbot.service.parsing.ParseProcessor;
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

    private GameRepository gameRepository;

    private ParseProcessor parseProcessor;

    private RuleProcessor ruleProcessor;

    private BetProcessor betProcessor;

    private RuleRepository ruleRepository;

    @Autowired
    public GameRestController(GameRepository gameRepository,
                              ParseProcessor parseProcessor,
                              RuleProcessor ruleProcessor,
                              BetProcessor betProcessor,
                              RuleRepository ruleRepository) {
        this.gameRepository = gameRepository;
        this.parseProcessor = parseProcessor;
        this.ruleProcessor = ruleProcessor;
        this.betProcessor = betProcessor;
        this.ruleRepository = ruleRepository;;
    }

    @GetMapping("/date/{dateText}")
    public ResponseEntity<List<Game>> getGamesByDate(@PathVariable String dateText) {
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

    @GetMapping("/since_date/{dateText}")
    public ResponseEntity<List<Game>> getGamesSinceDate(@PathVariable String dateText) {
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
            List<Game> games = parseProcessor.process();
            ruleProcessor.process(games);
            return new ResponseEntity<>(ruleProcessor.findAppropriateGames(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/make_bets")
    public ResponseEntity<List<Game>> processBets() {
        try {
            List<Game> ruleGames = ruleProcessor.findAppropriateGames();
            List<Rule> ruleList = ruleRepository.getByNames(Collections.singletonList("FW_SW"));
            List<Game> betMadeGames = betProcessor.process(ruleGames, ruleList);
            return new ResponseEntity<>(betMadeGames, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

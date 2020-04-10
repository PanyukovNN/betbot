package com.zylex.betbot.service.rest;

import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("game")
public class GameRestController {

    private GameRepository gameRepository;

    @Autowired
    public GameRestController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
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
}

package com.zylex.betbot;

import com.zylex.betbot.controller.Saver;
import com.zylex.betbot.model.Game;
import com.zylex.betbot.service.ParseProcessor;

import java.util.List;

public class OneXBetParserApplication {

    public static void main(String[] args) {
        int threads = 6;
        ParseProcessor parseProcessor = new ParseProcessor();
        List<Game> totalGames = parseProcessor.process(threads);
        Saver saver = new Saver();
        saver.processSaving(totalGames);
    }
}

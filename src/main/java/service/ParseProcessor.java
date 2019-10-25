package service;

import controller.Saver;
import model.Game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParseProcessor {

    public void process(DriverFactory driverFactory) throws IOException, InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(driverFactory.getThreads());

        File file = new File("results/leagues.txt");
        List<String> leagues = Files.readAllLines(file.toPath());

        List<Game> totalGames = new ArrayList<>();

        List<CallableGameParser> callableGameParsers = new ArrayList<>();
        for (String league : leagues) {
            callableGameParsers.add(new CallableGameParser(driverFactory, league));
        }
        List<Future<List<Game>>> futureGameParsers = service.invokeAll(callableGameParsers);

        for (Future<List<Game>> gameList : futureGameParsers) {
            List<Game> leagueGames = gameList.get();
            totalGames.addAll(leagueGames);
        }

        service.shutdown();

        Saver saver = new Saver();
        saver.processSaving(totalGames);
    }
}

package com.zylex.betbot.controller.repository;

import com.zylex.betbot.exception.BalanceRepositoryException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BetInfoRepository extends Repository {

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd;HH:mm");

    private File infoFile;

    {
        infoFile = new File("external-resources/info.csv");
        createFile(infoFile);
    }

    public LocalDateTime read() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)))) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(line, DATE_FORMATTER);
        } catch (IOException e) {
            throw new BalanceRepositoryException(e.getMessage(), e);
        }
    }

    public void write(LocalDateTime betTime) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(infoFile), StandardCharsets.UTF_8))) {
            writer.write(DATE_FORMATTER.format(betTime));
            writer.flush();
        } catch (IOException e) {
            throw new BalanceRepositoryException(e.getMessage(), e);
        }
    }
}

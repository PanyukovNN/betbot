package com.zylex.betbot.controller;

import com.zylex.betbot.exception.BetProcessorException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BalanceRepository extends Repository {

    private File balanceFile;

    {
        balanceFile = new File("results/bank.csv");
        createFile(balanceFile);
    }

    public int read() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(balanceFile)))) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                return -1;
            }
            return Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            throw new BetProcessorException(e.getMessage(), e);
        }
    }

    public void write(int balance) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(balanceFile), StandardCharsets.UTF_8))) {
            writer.write(balance);
        } catch (IOException e) {
            throw new BetProcessorException(e.getMessage(), e);
        }
    }
}

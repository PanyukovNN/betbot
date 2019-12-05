package com.zylex.betbot.controller.repository;

import com.zylex.betbot.exception.BalanceRepositoryException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BalanceRepository extends Repository {

    private File balanceFile;

    {
        balanceFile = new File("external-resources/bank.csv");
        createFile(balanceFile);
    }

    public int read() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(balanceFile)))) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                return -1;
            }
            return Integer.parseInt(line);
        } catch (IOException e) {
            throw new BalanceRepositoryException(e.getMessage(), e);
        }
    }

    public void write(int balance) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(balanceFile), StandardCharsets.UTF_8))) {
            writer.write(String.valueOf(balance));
            writer.flush();
        } catch (IOException e) {
            throw new BalanceRepositoryException(e.getMessage(), e);
        }
    }
}

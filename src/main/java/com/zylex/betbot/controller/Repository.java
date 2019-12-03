package com.zylex.betbot.controller;

import com.zylex.betbot.exception.GameRepositoryException;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public abstract class Repository {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new GameRepositoryException(e.getMessage(), e);
        }
    }
}

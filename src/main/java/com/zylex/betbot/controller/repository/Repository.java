package com.zylex.betbot.controller.repository;

import com.zylex.betbot.exception.RepositoryException;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public abstract class Repository {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }
}

package com.zylex.betbot.service;

/**
 * Specifies the day for parsing.
 */
public enum Day {
    TODAY(0),
    TOMORROW(1);
    
    public final int INDEX;
    
    Day(int INDEX) {
        this.INDEX = INDEX;
    }
}

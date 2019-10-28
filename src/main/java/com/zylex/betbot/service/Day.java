package com.zylex.betbot.service;

public enum Day {
    TODAY(0),
    TOMORROW(1);
    
    public final int INDEX;
    
    private Day(int INDEX) {
        this.INDEX = INDEX;
    }
}

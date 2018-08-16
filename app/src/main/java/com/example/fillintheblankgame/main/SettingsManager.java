package com.example.fillintheblankgame.main;

class SettingsManager {
    private static final SettingsManager ourInstance = new SettingsManager();

    static SettingsManager getInstance() {
        return ourInstance;
    }

    private SettingsManager() {
    }

    public String itemName = null;
    public int itemPosition = -1;
}

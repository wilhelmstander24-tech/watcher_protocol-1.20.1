package com.mchorror.watcherprotocol.config;

public class WatcherProtocolConfig {
    private boolean modEnabled = true;
    private boolean phaseOneEnabled = true;
    private boolean allowInGameConfig = true;

    public boolean isModEnabled() {
        return modEnabled;
    }

    public void setModEnabled(boolean modEnabled) {
        this.modEnabled = modEnabled;
    }

    public boolean isPhaseOneEnabled() {
        return phaseOneEnabled;
    }

    public void setPhaseOneEnabled(boolean phaseOneEnabled) {
        this.phaseOneEnabled = phaseOneEnabled;
    }

    public boolean isAllowInGameConfig() {
        return allowInGameConfig;
    }

    public void setAllowInGameConfig(boolean allowInGameConfig) {
        this.allowInGameConfig = allowInGameConfig;
    }
}
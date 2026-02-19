package com.mchorror.watcherprotocol.config;

import com.mchorror.watcherprotocol.phases.PhaseType;

public class WatcherProtocolConfig {
    private boolean modEnabled = true;
    private boolean phaseOneEnabled = true;
    private boolean phaseTwoEnabled = true;
    private boolean phaseThreeEnabled = true;
    private boolean phaseFourEnabled = true;
    private boolean phaseFiveEnabled = true;
    private boolean phaseSixEnabled = true;
    private boolean allowInGameConfig = true;
    private double interferenceIntensity = 1.0;
    private double interruptionFrequency = 1.0;
    private double worldDestructiveness = 0.5;

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

    public boolean isPhaseTwoEnabled() {
        return phaseTwoEnabled;
    }

    public void setPhaseTwoEnabled(boolean phaseTwoEnabled) {
        this.phaseTwoEnabled = phaseTwoEnabled;
    }

    public boolean isPhaseThreeEnabled() {
        return phaseThreeEnabled;
    }

    public void setPhaseThreeEnabled(boolean phaseThreeEnabled) {
        this.phaseThreeEnabled = phaseThreeEnabled;
    }

    public boolean isPhaseFourEnabled() {
        return phaseFourEnabled;
    }

    public void setPhaseFourEnabled(boolean phaseFourEnabled) {
        this.phaseFourEnabled = phaseFourEnabled;
    }

    public boolean isPhaseFiveEnabled() {
        return phaseFiveEnabled;
    }

    public void setPhaseFiveEnabled(boolean phaseFiveEnabled) {
        this.phaseFiveEnabled = phaseFiveEnabled;
    }

    public boolean isPhaseSixEnabled() {
        return phaseSixEnabled;
    }

    public void setPhaseSixEnabled(boolean phaseSixEnabled) {
        this.phaseSixEnabled = phaseSixEnabled;
    }

    public boolean isAllowInGameConfig() {
        return allowInGameConfig;
    }

    public void setAllowInGameConfig(boolean allowInGameConfig) {
        this.allowInGameConfig = allowInGameConfig;
    }

    public double getInterferenceIntensity() {
        return interferenceIntensity;
    }

    public void setInterferenceIntensity(double interferenceIntensity) {
        this.interferenceIntensity = clamp(interferenceIntensity, 0.0, 2.0);
    }

    public double getInterruptionFrequency() {
        return interruptionFrequency;
    }

    public void setInterruptionFrequency(double interruptionFrequency) {
        this.interruptionFrequency = clamp(interruptionFrequency, 0.25, 2.0);
    }

    public double getWorldDestructiveness() {
        return worldDestructiveness;
    }

    public void setWorldDestructiveness(double worldDestructiveness) {
        this.worldDestructiveness = clamp(worldDestructiveness, 0.0, 1.0);
    }

    public boolean isPhaseEnabled(PhaseType phaseType) {
        return switch (phaseType) {
            case PHASE_1 -> phaseOneEnabled;
            case PHASE_2 -> phaseTwoEnabled;
            case PHASE_3 -> phaseThreeEnabled;
            case PHASE_4 -> phaseFourEnabled;
            case PHASE_5 -> phaseFiveEnabled;
            case PHASE_6 -> phaseSixEnabled;
            case PHASE_7 -> false;
        };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
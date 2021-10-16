package org.elvincode.fall_detector_v2.models;

public class HistoryLogModel {
    private String fallState;   // The fall state of the fall (Non-Fatal or Fatal)
    private String location;    // Stores the current Location of the impact


    public HistoryLogModel(String fallState, String location) {
        this.fallState = fallState;
        this.location = location;
    }

    public HistoryLogModel() {
    }

    public String getFallState() {
        return fallState;
    }

    public void setFallState(String fallState) {
        this.fallState = fallState;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

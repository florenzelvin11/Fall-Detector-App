package org.elvincode.fall_detector_v2.models;

public class HistoryLogDisplay {
    private String dateTime;
    private String fallState;   // The fall state of the fall (Non-Fatal or Fatal)
    private String location;    // Stores the current Location of the impact


    public HistoryLogDisplay(String dateTime, String fallState, String location) {
        this.dateTime = dateTime;
        this.fallState = fallState;
        this.location = location;
    }

    public HistoryLogDisplay() {
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
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

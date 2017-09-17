package com.sepulchre.autocourt.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Booking {

    private boolean isRecurrent;
    private String locationId;
    private LocalDateTime startTime;
    private int duration;

    // e.g. 2017-09-23
    public static DateTimeFormatter url_formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd");

    // e.g. 2017-09-23-24-00
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd H:m");

    public Booking(boolean isRecurrent, String locationId, LocalDateTime startTime, int
            duration) {
        this.isRecurrent = isRecurrent;
        this.locationId = locationId;
        this.startTime = startTime;
        this.duration = duration;
    }

    public boolean isRecurrent() {
        return isRecurrent;
    }

    public void setRecurrent(boolean recurrent) {
        isRecurrent = recurrent;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "isRecurrent=" + isRecurrent +
                ", locationId='" + locationId + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}

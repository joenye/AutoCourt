package com.sepulchre.autocourt.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Booking implements Serializable {

    private boolean isRecurrent;
    private String locationId;
    private LocalDateTime startTime;
    private int duration;

    public static DateTimeFormatter url_formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd");

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd H:mm");// e.g. 2017-09-23-24-00

    public static DateTimeFormatter long_formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd H:mm:ss");

    public Booking() {
        // For Jackson
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return isRecurrent == booking.isRecurrent &&
                duration == booking.duration &&
                Objects.equals(locationId, booking.locationId) &&
                Objects.equals(startTime, booking.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isRecurrent, locationId, startTime, duration);
    }

    @Override
    public String toString() {
        return "Booking {" +
                "isRecurrent=" + isRecurrent +
                ", locationId='" + locationId + '\'' +
                ", startTime=" + formatter.format(startTime) +
                ", duration=" + duration +
                "}";
    }
}

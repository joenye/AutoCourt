package com.sepulchre.autocourt.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class Booking implements Serializable {

    private UUID id;
    private boolean isRecurrent;
    private Location location;
    private LocalDateTime startTime;
    private int duration;

    public enum Location {
        CLISSOLD_PARK("3473"),
        ASKE_GARDENS("3638");

        public String id;

        Location(String id) {
            this.id = id;
        }

        public static Location getById(String id) {
            for (Location l : values()) {
                if (l.id.equals(id)) return l;
            }
            return null;
        }
    }

    public static final DateTimeFormatter CLISSOLD_URL_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd");

    public static final DateTimeFormatter API_URL_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd-H:mm");// e.g. 2017-09-23-24:00

    public Booking() {
        // For Jackson
    }

    public Booking(boolean isRecurrent, Location location, LocalDateTime startTime, int
            duration) {
        this.id = UUID.randomUUID();
        this.isRecurrent = isRecurrent;
        this.location = location;
        this.startTime = startTime;
        this.duration = duration;
    }


    public Booking(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public boolean isRecurrent() {
        return isRecurrent;
    }

    public void setRecurrent(boolean recurrent) {
        isRecurrent = recurrent;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocationId(Location location) {
        this.location = location;
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
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Booking {" +
                "id=" + id +
                "isRecurrent=" + isRecurrent +
                ", location='" + location + '\'' +
                ", startTime=" + API_URL_FORMATTER.format(startTime) +
                ", duration=" + duration +
                "}";
    }
}

package com.alertsapp.app;

public class EventDuration {

    private String category;
    private String city;
    private String startTime;
    private String endTime;
    private long durationMinutes;

    public EventDuration(String category, String city,
                         String startTime, String endTime,
                         long durationMinutes) {
        this.category = category;
        this.city = city;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public String getCategory() {
        return category;
    }
}
package com.optazen.skillmatch.domain;

import java.time.LocalDate;

public class TimeBucket {
    private int id;
    private Resource resource;
    private LocalDate date;
    private int hours;

    private int dayIndex;

    public TimeBucket() {
    }

    public TimeBucket(int id, Resource resource, LocalDate date, int hours, int dayIndex) {
        this.id = id;
        this.resource = resource;
        this.date = date;
        this.hours = hours;
        this.dayIndex = dayIndex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    @Override
    public String toString() {
        return "'" + resource + "' at '" + date + "'";
    }
}

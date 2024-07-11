package com.optazen.skillmatch.api;

import java.util.List;

public class Calendar {
    private String id;
    private String name;
    private boolean unspecifiedTimeIsWorking;
    private List<Interval> intervals;

    public Calendar() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUnspecifiedTimeIsWorking() {
        return unspecifiedTimeIsWorking;
    }

    public void setUnspecifiedTimeIsWorking(boolean unspecifiedTimeIsWorking) {
        this.unspecifiedTimeIsWorking = unspecifiedTimeIsWorking;
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<Interval> intervals) {
        this.intervals = intervals;
    }
}

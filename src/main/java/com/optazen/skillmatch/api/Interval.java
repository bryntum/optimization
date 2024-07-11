package com.optazen.skillmatch.api;

public class Interval {
    private String name;
    private String recurrentStartDate;
    private String recurrentEndDate;
    private boolean isWorking;

    public Interval() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecurrentStartDate() {
        return recurrentStartDate;
    }

    public void setRecurrentStartDate(String recurrentStartDate) {
        this.recurrentStartDate = recurrentStartDate;
    }

    public String getRecurrentEndDate() {
        return recurrentEndDate;
    }

    public void setRecurrentEndDate(String recurrentEndDate) {
        this.recurrentEndDate = recurrentEndDate;
    }

    public boolean getIsWorking() {
        return isWorking;
    }

    public void setIsWorking(boolean working) {
        isWorking = working;
    }
}

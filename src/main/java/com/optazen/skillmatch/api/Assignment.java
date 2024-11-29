package com.optazen.skillmatch.api;

public class Assignment {
    private int id;
    private String $PhantomId;
    private int eventId;
    private int resourceId;

    public Assignment() {
    }

    public int getId() {
        return id;
    }

    public String get$PhantomId() {
        return $PhantomId;
    }

    public void set$PhantomId(String $PhantomId) {
        this.$PhantomId = $PhantomId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
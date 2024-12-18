package com.optazen.skillmatch.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Assignment {
    private String id;
    @JsonProperty("$PhantomId")
    private String phantomId;
    private int eventId;
    private int resourceId;

    public Assignment() {
    }

    public String getId() {
        return id;
    }

    public String getPhantomId() {
        return phantomId;
    }

    public void setPhantomId(String phantomId) {
        this.phantomId = phantomId;
    }

    public void setId(String id) {
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
package com.optazen.skillmatch.api;

import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;

public class Sync {
    private String type;
    private long requestId;
    private Crud<Event> events;
    private Crud<Resource> resources;

    public Sync() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public Crud<Event> getEvents() {
        return events;
    }

    public void setEvents(Crud<Event> events) {
        this.events = events;
    }

    public Crud<Resource> getResources() {
        return resources;
    }

    public void setResources(Crud<Resource> resources) {
        this.resources = resources;
    }
}

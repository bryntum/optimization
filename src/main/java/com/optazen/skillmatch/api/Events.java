package com.optazen.skillmatch.api;

import com.optazen.skillmatch.domain.Event;

import java.util.List;

public class Events {
    private List<Event> rows;

    public Events() {
    }

    public List<Event> getRows() {
        return rows;
    }

    public void setRows(List<Event> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "Events{" +
                "rows=" + rows +
                '}';
    }
}

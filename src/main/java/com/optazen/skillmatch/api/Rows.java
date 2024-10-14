package com.optazen.skillmatch.api;

import java.util.List;

public class Rows<T> {
    private List<T> rows;

    // Getters and setters
    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
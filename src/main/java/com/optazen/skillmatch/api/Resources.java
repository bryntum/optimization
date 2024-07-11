package com.optazen.skillmatch.api;

import com.optazen.skillmatch.domain.Resource;

import java.util.List;

public class Resources {
    private List<Resource> rows;

    public Resources() {
    }

    public List<Resource> getRows() {
        return rows;
    }

    public void setRows(List<Resource> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        return "Resources{" +
                "rows=" + rows +
                '}';
    }
}



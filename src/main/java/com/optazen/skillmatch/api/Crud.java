package com.optazen.skillmatch.api;

import java.util.ArrayList;
import java.util.List;

public class Crud<T> {
    private List<T> added = new ArrayList<>();
    private List<T> updated = new ArrayList<>();
    private List<T> removed = new ArrayList<>();

    public Crud() {
    }

    public List<T> getAdded() {
        return added;
    }

    public void setAdded(List<T> added) {
        this.added = added;
    }

    public List<T> getUpdated() {
        return updated;
    }

    public void setUpdated(List<T> updated) {
        this.updated = updated;
    }

    public List<T> getRemoved() {
        return removed;
    }

    public void setRemoved(List<T> removed) {
        this.removed = removed;
    }
}

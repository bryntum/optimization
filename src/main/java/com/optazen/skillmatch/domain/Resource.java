package com.optazen.skillmatch.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Resource {
    @PlanningId
    private int id;
    private String name;
    private String role;
    private String calendar;
    private String type;
    private String image;
    private String eventColor;
    private List<Integer> skills;

    @JsonProperty("$PhantomId")
    private String phantomId;

    public Resource() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getSkills() {
        return skills;
    }

    public void setSkills(List<Integer> skills) {
        this.skills = skills;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEventColor() {
        return eventColor;
    }

    public void setEventColor(String eventColor) {
        this.eventColor = eventColor;
    }

    public String getPhantomId() {
        return phantomId;
    }

    public void setPhantomId(String phantomId) {
        this.phantomId = phantomId;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    public void update(Resource resourceUpdated) {
        if (resourceUpdated.getName() != null) {
            this.setName(resourceUpdated.getName());
        }
        if(resourceUpdated.getRole() != null) {
            this.setRole(resourceUpdated.getRole());
        }
        if(resourceUpdated.getSkills() != null) {
            this.setSkills(resourceUpdated.getSkills());
        }
    }
}

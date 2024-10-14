package com.optazen.skillmatch.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

@PlanningEntity
public class Event {
    @PlanningId
    private int id;
    private String name;
    private String licensePlate;
    private Integer duration;
    private List<Integer> skills;

    @PlanningVariable
    @JsonIgnore
    private TimeBucket timeBucket;
    private LocalDateTime startDate;
    private Integer resourceId;

    @PlanningPin
    @JsonIgnore
    private boolean pinned = true;

    public Event() {
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

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<Integer> getSkills() {
        return skills;
    }

    public void setSkills(List<Integer> skills) {
        this.skills = skills;
    }

    public LocalDateTime getStartDate() {
        if (timeBucket == null) {
            return startDate;
        }
        return timeBucket.getDate().atTime(8, 0);
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public Integer getResourceId() {
        if (timeBucket == null) {
            return resourceId;
        }
        return timeBucket.getResource().getId();
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public TimeBucket getTimeBucket() {
        return timeBucket;
    }

    public void setTimeBucket(TimeBucket timeBucket) {
        this.timeBucket = timeBucket;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Override
    public String toString() {
        return "'" + name + " (" + id + ")' with " + timeBucket;
    }

    public void update(Event eventUpdated) {
        if (eventUpdated.getStartDate() != null) {
            this.setStartDate(eventUpdated.getStartDate());
        }
        if (eventUpdated.getDuration() != null) {
            this.setDuration(eventUpdated.getDuration());
        }
        if(eventUpdated.getResourceId() != null) {
            this.setResourceId(eventUpdated.getResourceId());
        }
    }
}

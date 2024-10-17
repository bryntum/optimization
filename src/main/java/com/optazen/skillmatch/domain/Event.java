package com.optazen.skillmatch.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.optazen.skillmatch.solver.EventDifficultyComparator;
import com.optazen.skillmatch.solver.ResourceStrengthComparator;
import jakarta.annotation.Nullable;

import java.time.LocalDateTime;
import java.util.List;

@PlanningEntity(difficultyComparatorClass = EventDifficultyComparator.class)
public class Event {
    @PlanningId
    private int id;
    private String name;
    private String licensePlate;
    private Integer duration;
    private List<Integer> skills;

    @PlanningVariable
    private LocalDateTime startDate;
    @PlanningVariable(strengthComparatorClass = ResourceStrengthComparator.class)
    private Resource resource;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    private Integer resourceId;

    @PlanningPin
    private boolean manuallyScheduled = false;

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
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    @JsonIgnore
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Integer getResourceId() {
        return (resource == null) ? resourceId : Integer.valueOf(resource.getId());
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public boolean isManuallyScheduled() {
        return manuallyScheduled;
    }

    public void setManuallyScheduled(boolean manuallyScheduled) {
        this.manuallyScheduled = manuallyScheduled;
    }

    @Override
    public String toString() {
        return "'" + name + " (" + id + ")' at " + startDate;
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
        this.manuallyScheduled = eventUpdated.isManuallyScheduled();
    }

    @JsonIgnore
    public LocalDateTime getEndDate() {
        return startDate == null ? null : startDate.plusHours(duration);
    }
}

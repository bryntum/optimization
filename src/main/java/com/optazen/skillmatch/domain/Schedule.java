package com.optazen.skillmatch.domain;

import ai.timefold.solver.core.api.domain.solution.*;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.optazen.skillmatch.solver.ConstraintParameters;

import java.time.LocalDateTime;
import java.util.List;

@PlanningSolution
public class Schedule {
    @PlanningEntityCollectionProperty
    List<Event> events;
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    List<Resource> resources;
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    List<LocalDateTime> startDates;
    @ProblemFactProperty
    ConstraintParameters constraintParameters;

    @PlanningScore
    private HardSoftScore score;

    public Schedule() {
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public void setStartDates(List<LocalDateTime> startDates) {
        this.startDates = startDates;
    }

    public void setConstraintParameters(ConstraintParameters constraintParameters) {
        this.constraintParameters = constraintParameters;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "events=" + events + "}";
    }
}


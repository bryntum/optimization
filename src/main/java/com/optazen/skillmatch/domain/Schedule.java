package com.optazen.skillmatch.domain;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@PlanningSolution
public class Schedule {
    @PlanningEntityCollectionProperty
    List<Event> events;
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    List<TimeBucket> timeBuckets;

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

    public List<TimeBucket> getTimeBuckets() {
        return timeBuckets;
    }

    public void setTimeBuckets(List<TimeBucket> timeBuckets) {
        this.timeBuckets = timeBuckets;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "events=" + events + "}";
    }
}

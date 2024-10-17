package com.optazen.skillmatch.domain;

import ai.timefold.solver.core.api.domain.solution.*;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.optazen.skillmatch.solver.ConstraintParameters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    // Function to generate LocalDateTime for every hour, except for weekends
    public static List<LocalDateTime> generateHourlyDateTimes(LocalDate startDate, LocalDate endDate, Set<Integer> workingDays,
                                                              LocalTime startTimeMorning, LocalTime endTimeEvening) {
        List<LocalDateTime> dateTimes = new ArrayList<>();

        LocalDate currentDate = startDate;

        // Loop through each day between start and end date
        while (!currentDate.isAfter(endDate)) {
            // Skip weekends (Saturday and Sunday)
            if (workingDays.contains(currentDate.getDayOfWeek().getValue())) {
                // Loop through each hour between start time and end time
                LocalTime currentTime = startTimeMorning;
                while (!currentTime.isAfter(endTimeEvening)) {
                    dateTimes.add(LocalDateTime.of(currentDate, currentTime));
                    currentTime = currentTime.plusHours(1);  // Move forward by 1 hour
                }
            }
            currentDate = currentDate.plusDays(1);  // Move to the next day
        }
        return dateTimes;
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
        this.setStartDates(generateHourlyDateTimes(constraintParameters.startDate(), constraintParameters.endDate(), constraintParameters.workingDays(), constraintParameters.startTime(), constraintParameters.endTime()));
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "events=" + events + "}";
    }
}


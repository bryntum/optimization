package com.optazen.skillmatch.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optazen.skillmatch.domain.*;
import com.optazen.skillmatch.solver.ConstraintParameters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Data {
    private boolean success = true;
    private Project project;

    private Rows<Calendar> calendars;

    private Rows<Event> events;
    private Rows<Resource> resources;

    private Rows<Event> unplanned;

    private Rows<Skill> skills;

    @JsonIgnore
    private LocalDate startDate = LocalDate.of(2024, 11, 4);
    @JsonIgnore
    private LocalDate endDate = LocalDate.of(2024, 11, 9);

    public Data() {
    }

    public Data(Schedule schedule) {
        setSchedule(schedule);
    }

    @JsonIgnore
    public Schedule getSchedule() {
        Schedule schedule = new Schedule();
        schedule.setConstraintParameters(new ConstraintParameters(startDate));
        schedule.setResources(resources.getRows());
        schedule.setStartDates(generateHourlyDateTimes(startDate, endDate, LocalTime.of(8,0), LocalTime.of(18,0)));

        events.getRows().forEach(event -> event.setResource(findResource(event.getResourceId())));
        List<Event> combinedEvents = new ArrayList<>(events.getRows());
        unplanned.getRows().forEach(event -> event.setManuallyScheduled(false));
        combinedEvents.addAll(unplanned.getRows());
        schedule.setEvents(combinedEvents);

        return schedule;
    }

    // Function to generate LocalDateTime for every hour, except for weekends
    public static List<LocalDateTime> generateHourlyDateTimes(LocalDate startDate, LocalDate endDate,
                                                              LocalTime startTimeMorning, LocalTime endTimeEvening) {
        List<LocalDateTime> dateTimes = new ArrayList<>();

        LocalDate currentDate = startDate;

        // Loop through each day between start and end date
        while (!currentDate.isAfter(endDate)) {
            // Skip weekends (Saturday and Sunday)
            if (!(currentDate.getDayOfWeek().getValue() == 6 || currentDate.getDayOfWeek().getValue() == 7)) {
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

    public void setSchedule(Schedule schedule) {
        if(events == null) {
            events = new Rows<>();
        }
        if (unplanned == null) {
            unplanned = new Rows<>();
        }
        this.events.setRows(schedule.getEvents().stream().filter(event -> event.getStartDate() != null).collect(Collectors.toList()));
        this.unplanned.setRows(schedule.getEvents().stream().filter(event -> event.getStartDate() == null).collect(Collectors.toList()));
    }

    private Resource findResource(int resourceId) {
        return this.resources.getRows().stream().filter(resource -> resourceId == resource.getId()).findFirst().orElse(null);
    }

    public Rows<Event> getEvents() {
        return events;
    }

    public void setEvents(Rows<Event> events) {
        this.events = events;
    }

    public Rows<Resource> getResources() {
        return resources;
    }

    public void setResources(Rows<Resource> resources) {
        this.resources = resources;
    }

    public Rows<Event> getUnplanned() {
        return unplanned;
    }

    public void setUnplanned(Rows<Event> unplanned) {
        this.unplanned = unplanned;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Rows<Calendar> getCalendars() {
        return calendars;
    }

    public void setCalendars(Rows<Calendar> calendars) {
        this.calendars = calendars;
    }

    public Rows<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Rows<Skill> skills) {
        this.skills = skills;
    }

    @Override
    public String toString() {
        return "Data{" +
                "events=" + events +
                ", resources=" + resources +
                ", unplanned=" + unplanned +
                '}';
    }
}

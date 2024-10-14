package com.optazen.skillmatch.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.domain.TimeBucket;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private List<TimeBucket> timeBuckets = new ArrayList<>();
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
        LocalDate currentDate = startDate;
        int timeBucketCounter = 0;
        int dayIndexCounter = 0;
        while (endDate.isAfter(currentDate)) {
            for (Resource resource : resources.getRows()) {
                this.timeBuckets.add(new TimeBucket(timeBucketCounter++, resource, currentDate, 8, dayIndexCounter));
            }
            currentDate = currentDate.plusDays(1);
            dayIndexCounter++;
        }

        Schedule schedule = new Schedule();
        schedule.setTimeBuckets(timeBuckets);

        events.getRows().forEach(event -> event.setTimeBucket(findTimeBucket(event.getStartDate(), event.getResourceId())));
        List<Event> combinedEvents = new ArrayList<>(events.getRows());
        unplanned.getRows().forEach(event -> event.setPinned(false));
        combinedEvents.addAll(unplanned.getRows());
        schedule.setEvents(combinedEvents);

        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        if(events == null) {
            events = new Rows<>();
        }
        if (unplanned == null) {
            unplanned = new Rows<>();
        }
        if (resources == null) {
            resources = new Rows<>();
        }
        this.resources.setRows(schedule.getTimeBuckets().stream().map(TimeBucket::getResource).distinct().collect(Collectors.toList()));
        this.events.setRows(schedule.getEvents().stream().filter(event -> event.getTimeBucket() != null).collect(Collectors.toList()));
        this.unplanned.setRows(schedule.getEvents().stream().filter(event -> event.getTimeBucket() == null).collect(Collectors.toList()));
    }

    private TimeBucket findTimeBucket(LocalDateTime startDate, int resourceId) {
        return this.timeBuckets.stream().filter(timeBucket -> startDate.toLocalDate().equals(timeBucket.getDate()) && resourceId == timeBucket.getResource().getId()).findFirst().orElse(null);
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

    public List<TimeBucket> getTimeBuckets() {
        return timeBuckets;
    }

    public void setTimeBuckets(List<TimeBucket> timeBuckets) {
        this.timeBuckets = timeBuckets;
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

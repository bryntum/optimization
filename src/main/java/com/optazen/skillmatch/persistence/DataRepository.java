package com.optazen.skillmatch.persistence;

import com.optazen.skillmatch.api.Assignment;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.service.ScoreAnalysisService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@ApplicationScoped
public class DataRepository {
    private static final Logger log = LoggerFactory.getLogger(DataRepository.class);
    @Inject
    ScoreAnalysisService scoreAnalysisService;
    private Data data;
    private static AtomicInteger counter;


    public Optional<Data> solution() {
        return Optional.ofNullable(data);
    }

    public Data update(Data data) {
        this.data = data;
        // Update counter to include resource IDs
        Optional<Integer> maxEventId = Stream.concat(data.getEvents().getRows().stream(), data.getUnplanned().getRows().stream())
            .map(Event::getId).max(Integer::compareTo);
        Optional<Integer> maxResourceId = data.getResources().getRows().stream()
            .map(Resource::getId).max(Integer::compareTo);
        Optional<Integer> maxId = Stream.of(maxEventId, maxResourceId)
            .filter(Optional::isPresent).map(Optional::get)
            .max(Integer::compareTo);
        counter = new AtomicInteger(maxId.map(i -> i + 1).orElse(0));
        this.data.setScoreAnalysis(scoreAnalysisService.analysis(data.getSchedule()));
        return data;
    }

    public Data update(Schedule schedule) {
        this.data.setSchedule(schedule);
        update(data);
        return data;
    }

    public boolean update(Event eventUpdated) {
        Optional<Event> optionalEvent = this.data.getEvents().getRows().stream().filter(event -> event.getId() == eventUpdated.getId()).findFirst();
        if (optionalEvent.isPresent()) {
            optionalEvent.get().update(eventUpdated);
            return true;
        } else {
            return false;
        }
    }

    public Event add(Event eventAdded) {
        eventAdded.setId(counter.getAndIncrement());

        Optional<Resource> foundResource = data.getResources().getRows().stream().filter(resource -> resource.getId() == eventAdded.getResourceId()).findFirst();
        if(foundResource.isPresent() && eventAdded.getStartDate() != null) {
            eventAdded.setResource(foundResource.get());
            data.getEvents().getRows().add(eventAdded);
            return eventAdded;
        }

        eventAdded.setResource(null);
        eventAdded.setStartDate(null);
        data.getUnplanned().getRows().add(eventAdded);

        return eventAdded;
    }
        
    public Event addUnplanned(Event eventAdded) {
        eventAdded.setId(counter.getAndIncrement());

        eventAdded.setResource(null);
        eventAdded.setStartDate(null);

        data.getUnplanned().getRows().add(eventAdded);

        return eventAdded;
    }

    public boolean deleteUnplanned(Integer eventId) {
        return data.getUnplanned().getRows().removeIf(event -> event.getId() == eventId);
    }

    public boolean deleteEvent(Integer eventId) {
        return this.data.getEvents().getRows().removeIf(event -> event.getId() == eventId);
    }

    public boolean update(Resource resourceUpdated) {
        Optional<Resource> optionalResource = this.data.getResources().getRows().stream()
                .filter(resource -> resource.getId() == resourceUpdated.getId())
                .findFirst();
        if (optionalResource.isPresent()) {
            optionalResource.get().update(resourceUpdated);
            return true;
        } else {
            return false;
        }
    }

    public List<Event> deleteResource(Integer resourceId) {
        List<Event> unplannedEventsForResource = new ArrayList<>();
        ListIterator<Resource> iterator = this.data.getResources().getRows().listIterator();
        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            if (resource.getId() == resourceId) {
                // Unplan events => no resource id + no start date / time
                List<Event> eventList = this.data.getEvents().getRows().stream()
                        .filter(event -> Objects.equals(event.getResourceId(), resourceId)).toList();

                eventList.forEach(event -> {
                    event.setResource(null);
                    event.setStartDate(null);
                    this.data.getUnplanned().getRows().add(event);
                    unplannedEventsForResource.add(event);
                });

                // remove events from the planned ones
                if (!this.data.getEvents().getRows().removeAll(eventList)) {
                    log.error("Removal of planned events was not possible");
                }

                // remove the resource
                iterator.remove();

                // return the list of events, so that it can be returned in the REST API
                return unplannedEventsForResource;
            }
        }

        return unplannedEventsForResource;
    }

    public boolean update(Assignment assignment) {
        Optional<Event> event = this.data.getEvents().getRows().stream().filter(e -> e.getId() == assignment.getEventId()).findFirst();
        Optional<Resource> resource = this.data.getResources().getRows().stream().filter(r -> r.getId() == assignment.getResourceId()).findFirst();
        if (event.isPresent() && resource.isPresent()) {
            event.get().setResource(resource.get());
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteAssignment(int eventId) {
        return this.data.getEvents().getRows().stream().anyMatch(event -> {
            if (event.getId() == eventId) {
                event.setResource(null);
                return true;
            }
            return false;
        });
    }

    public Assignment addAssignment(Assignment assignment) {
        assignment.setId(counter.getAndIncrement());

        Optional<Event> event = this.data.getUnplanned().getRows().stream().filter(e -> e.getId() == assignment.getEventId()).findFirst();
        Optional<Resource> resource = this.data.getResources().getRows().stream().filter(r -> r.getId() == assignment.getResourceId()).findFirst();
        if (event.isPresent() && resource.isPresent()) {
            event.get().setResource(resource.get());
            return assignment;
        } else {
            return null;
        }
    }

    public Resource addResource(Resource resource) {
        resource.setId(counter.getAndIncrement());
        data.getResources().getRows().add(resource);
        return resource;
    }
}

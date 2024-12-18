package com.optazen.skillmatch.persistence;

import com.optazen.skillmatch.api.Assignment;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.bootstrap.StartupInitializer;
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
    @Inject
    StartupInitializer startupInitializer;
    private final Map<UUID, Data> dataMap = new HashMap<>();
    private static AtomicInteger counter;

    public Optional<Data> solution(UUID scheduleId) {
        Data data = dataMap.get(scheduleId);
        if(data == null) {
            data = updateWithData(scheduleId, startupInitializer.data());
        }
        return Optional.ofNullable(data);
    }

    public Data updateWithData(UUID scheduleId, Data data) {
        // Update counter to include resource IDs
        Optional<Integer> maxEventId = Stream.concat(data.getEvents().getRows().stream(), data.getUnplanned().getRows().stream())
            .map(Event::getId).max(Integer::compareTo);
        Optional<Integer> maxResourceId = data.getResources().getRows().stream()
            .map(Resource::getId).max(Integer::compareTo);
        Optional<Integer> maxId = Stream.of(maxEventId, maxResourceId)
            .filter(Optional::isPresent).map(Optional::get)
            .max(Integer::compareTo);
        counter = new AtomicInteger(maxId.map(i -> i + 1).orElse(0));
        data.setScoreAnalysis(scoreAnalysisService.analysis(data.getSchedule()));

        dataMap.put(scheduleId, data);
        return data;
    }

    public Data updateWithSchedule(UUID scheduleId, Schedule schedule) {
        Data data = dataMap.get(scheduleId);
        if (data == null) {
            return null;
        }
        data.setSchedule(schedule);
        updateWithData(scheduleId, data);
        return data;
    }

    public boolean updateEvent(UUID scheduleId, Event eventUpdated) {
        Data data = dataMap.get(scheduleId);
        if(data == null) {
            return false;
        }
        Optional<Event> optionalEvent = data.getEvents().getRows().stream().filter(event -> event.getId() == eventUpdated.getId()).findFirst();
        if (optionalEvent.isPresent()) {
            optionalEvent.get().update(eventUpdated);
            return true;
        } else {
            return false;
        }
    }

    public Event addEvent(UUID scheduleId, Event eventAdded) {
        eventAdded.setId(counter.getAndIncrement());

        Data data = dataMap.get(scheduleId);
        if(data == null) {
            return null;
        }

        Optional<Resource> foundResource = data.getResources().getRows().stream().filter(resource -> resource.getId() == eventAdded.getResourceId()).findFirst();
        if(foundResource.isPresent() && eventAdded.getStartDate() != null) {
            eventAdded.setResource(foundResource.get());
            data.getEvents().getRows().add(eventAdded);
        } else {
            eventAdded.setResource(null);
            eventAdded.setStartDate(null);
            data.getUnplanned().getRows().add(eventAdded);
        }
        return eventAdded;
    }
        
    public Event addUnplanned(UUID scheduleId, Event eventAdded) {
        eventAdded.setId(counter.getAndIncrement());

        eventAdded.setResource(null);
        eventAdded.setStartDate(null);

        dataMap.get(scheduleId).getUnplanned().getRows().add(eventAdded);

        return eventAdded;
    }

    public boolean deleteUnplanned(UUID scheduleId, Integer eventId) {
        return dataMap.get(scheduleId).getUnplanned().getRows().removeIf(event -> event.getId() == eventId);
    }

    public boolean deleteEvent(UUID scheduleId, Integer eventId) {
        return dataMap.get(scheduleId).getEvents().getRows().removeIf(event -> event.getId() == eventId);
    }

    public boolean updateResource(UUID scheduleId, Resource resourceUpdated) {
        Optional<Resource> optionalResource = dataMap.get(scheduleId).getResources().getRows().stream()
                .filter(resource -> resource.getId() == resourceUpdated.getId())
                .findFirst();
        if (optionalResource.isPresent()) {
            optionalResource.get().update(resourceUpdated);
            return true;
        } else {
            return false;
        }
    }

    public List<Event> deleteResource(UUID scheduleId, Integer resourceId) {
        List<Event> unplannedEventsForResource = new ArrayList<>();
        Data data = dataMap.get(scheduleId);
        ListIterator<Resource> iterator = data.getResources().getRows().listIterator();
        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            if (resource.getId() == resourceId) {
                // Unplan events => no resource id + no start date / time
                List<Event> eventList = data.getEvents().getRows().stream()
                        .filter(event -> Objects.equals(event.getResourceId(), resourceId)).toList();

                eventList.forEach(event -> {
                    event.setResource(null);
                    event.setStartDate(null);
                    data.getUnplanned().getRows().add(event);
                    unplannedEventsForResource.add(event);
                });

                // remove events from the planned ones
                if (!data.getEvents().getRows().removeAll(eventList)) {
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

    public boolean update(UUID scheduleId, Assignment assignment) {
        Data data = dataMap.get(scheduleId);
        Optional<Event> event = data.getEvents().getRows().stream().filter(e -> e.getId() == assignment.getEventId()).findFirst();
        Optional<Resource> resource = data.getResources().getRows().stream().filter(r -> r.getId() == assignment.getResourceId()).findFirst();
        if (event.isPresent() && resource.isPresent()) {
            event.get().setResource(resource.get());
            return true;
        } else {
            return false;
        }
    }

    public Resource addResource(UUID scheduleId, Resource resource) {
        resource.setId(counter.getAndIncrement());
        dataMap.get(scheduleId).getResources().getRows().add(resource);
        return resource;
    }
}

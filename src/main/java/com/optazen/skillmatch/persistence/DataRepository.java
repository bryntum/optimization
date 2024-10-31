package com.optazen.skillmatch.persistence;

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

@ApplicationScoped
public class DataRepository {
    private static final Logger log = LoggerFactory.getLogger(DataRepository.class);
    @Inject
    ScoreAnalysisService scoreAnalysisService;
    private Data data;

    public Optional<Data> solution() {
        return Optional.ofNullable(data);
    }

    public Data update(Data data) {
        this.data = data;
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
}

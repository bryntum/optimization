package com.optazen.skillmatch.persistence;

import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.Schedule;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class DataRepository {
    private Data data;

    public Optional<Data> solution() {
        return Optional.ofNullable(data);
    }

    public Data update(Data data) {
        this.data = data;
        return data;
    }

    public Data update(Schedule schedule) {
        this.data.setSchedule(schedule);
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
        Optional<Resource> optionalResource = this.data.getResources().getRows().stream().filter(resource -> resource.getId() == resourceUpdated.getId()).findFirst();
        if (optionalResource.isPresent()) {
            optionalResource.get().update(resourceUpdated);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteResource(Integer resourceId) {
        ListIterator<Resource> iterator = this.data.getResources().getRows().listIterator();
        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            if (resource.getId() == resourceId) {
                // Unplan events => no resource id + no start date / time
                this.data.getEvents().getRows().stream().filter(event -> Objects.equals(event.getResourceId(), resourceId)).forEach(event -> {
                    event.setResourceId(null);
                    event.setStartDate(null);
                    this.data.getUnplanned().getRows().add(event);
                });

                this.data.getEvents().getRows().removeIf(event -> Objects.equals(event.getResourceId(), resourceId));

                iterator.remove();
                return true;
            }
        }

        return false;
    }
}

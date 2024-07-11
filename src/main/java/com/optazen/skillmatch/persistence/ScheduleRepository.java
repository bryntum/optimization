package com.optazen.skillmatch.persistence;

import com.optazen.skillmatch.domain.Schedule;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ScheduleRepository {

    private Schedule schedule;

    public Optional<Schedule> solution() {
        return Optional.ofNullable(schedule);
    }

    public Schedule update(Schedule schedule) {
        this.schedule = schedule;
        return schedule;
    }
}

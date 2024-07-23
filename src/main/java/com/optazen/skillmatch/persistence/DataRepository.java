package com.optazen.skillmatch.persistence;

import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.domain.Schedule;
import jakarta.enterprise.context.ApplicationScoped;

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

}

package com.optazen.skillmatch.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.persistence.ScheduleRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@ApplicationScoped
public class StartupInitializer {
    @Inject
    ObjectMapper objectMapper;

    @Inject
    ScheduleRepository scheduleRepository;

    public Schedule data(@Observes StartupEvent startupEvent) throws URISyntaxException, IOException {
        Data data = objectMapper.readValue(new URI("file:src/main/resources/data.json").toURL(), Data.class);
        return scheduleRepository.update(data.getSchedule());
    }
}
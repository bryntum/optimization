package com.optazen.skillmatch.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.persistence.DataRepository;
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
    DataRepository dataRepository;

    public Data data(@Observes StartupEvent startupEvent) throws URISyntaxException, IOException {
        Data data = objectMapper.readValue(new URI("file:src/main/resources/data.json").toURL(), Data.class);
        return dataRepository.update(data);
    }
}
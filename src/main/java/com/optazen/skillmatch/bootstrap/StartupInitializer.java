package com.optazen.skillmatch.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optazen.skillmatch.api.Data;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@ApplicationScoped
public class StartupInitializer {
    @Inject
    ObjectMapper objectMapper;

    public Data data() {
        Data data;
        try {
            data = objectMapper.readValue(new URI("file:src/main/resources/data.json").toURL(), Data.class);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return data;
    }
}
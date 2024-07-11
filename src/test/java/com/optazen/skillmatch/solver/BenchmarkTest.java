package com.optazen.skillmatch.solver;

import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.domain.Schedule;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@QuarkusTest
@EnabledIfSystemProperty(named = "slowly", matches = "true")
public class BenchmarkTest {
    @Inject
    PlannerBenchmarkFactory benchmarkFactory;

    @Inject
    ObjectMapper objectMapper;

    @Test
    public void benchmark() throws IOException, URISyntaxException {
        Schedule schedule = objectMapper.readValue(new URI("file:src/main/resources/data.json").toURL(), Data.class).getSchedule();
        benchmarkFactory.buildPlannerBenchmark(schedule).benchmarkAndShowReportInBrowser();
    }
}

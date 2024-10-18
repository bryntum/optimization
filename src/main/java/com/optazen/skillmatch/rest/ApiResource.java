package com.optazen.skillmatch.rest;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optazen.skillmatch.api.Crud;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.api.Sync;
import com.optazen.skillmatch.bootstrap.StartupInitializer;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.persistence.DataRepository;
import com.optazen.skillmatch.websocket.TimefoldWebsocket;
import io.quarkus.runtime.StartupEvent;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Path("/api")
public class ApiResource {
    public static final Long SINGLETON_SCHEDULE_ID = 1L;
    protected static final Logger logger = LoggerFactory.getLogger(ApiResource.class);

    @Inject
    SolverManager<Schedule, Long> solverManager;
    @Inject
    SolutionManager<Schedule, HardSoftScore> solutionManager;

    @Inject
    TimefoldWebsocket timefoldWebsocket;

    @Inject
    DataRepository dataRepository;

    @Inject
    StartupInitializer startupInitializer;

    @Inject
    ObjectMapper objectMapper;


    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Data update(Data data) {
        return dataRepository.update(data);
    }

    @POST
    @Path("/sync")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sync(Sync sync) {
        boolean allSucceded = true;
        List<Event> unplannedEvents = new ArrayList<>();
        Crud<Event> events = sync.getEvents();
        if (events != null) {
            allSucceded &= events.getUpdated().stream().allMatch(event -> dataRepository.update(event));
            allSucceded &= events.getRemoved().stream().allMatch(event -> dataRepository.deleteEvent(event.getId()));
            events.getAdded().forEach(event -> logger.error("Adding Events is not yet implemented"));
        }

        Crud<Resource> resources = sync.getResources();
        if (resources != null) {
            allSucceded &= resources.getUpdated().stream().allMatch(resource -> dataRepository.update(resource));
            unplannedEvents = resources.getRemoved().stream().map(resource -> dataRepository.deleteResource(resource.getId())).flatMap(Collection::stream).toList();
            resources.getAdded().forEach(event -> logger.error("Adding Resources is not yet implemented"));
        }

        Map<String, Object> jsonResponseObject = new HashMap<>();
        jsonResponseObject.put("success", allSucceded);
        jsonResponseObject.put("requestId", sync.getRequestId());

        if (!unplannedEvents.isEmpty()) {
            jsonResponseObject.put("unplanned", Collections.singletonMap("rows",
                    unplannedEvents.stream()
                            .map(event -> objectMapper.convertValue(event, Map.class))
                            .collect(Collectors.toList())));
        }

        return allSucceded ? Response.ok(jsonResponseObject).build() : Response.serverError().build();
    }

    private Map<String, Object> createSection(String phantomId, String id) {
        Map<String, Object> section = new HashMap<>();
        section.put("rows", Collections.singletonList(new HashMap<String, Object>() {{
            put("$PhantomId", phantomId);
            put("id", id);
        }}));
        return section;
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Data reset() throws URISyntaxException, IOException {
        return startupInitializer.data(new StartupEvent());
    }

    @POST
    @Path("/solve")
    @Consumes(MediaType.APPLICATION_JSON)
    public void solve() {
        // Submit the problem to start solving
        Optional<Data> data = dataRepository.solution();
        Schedule solution;
        SolverJob<Schedule, Long> solverJob = solverManager.solveAndListen(
                SINGLETON_SCHEDULE_ID,
                data.orElseThrow().getSchedule(),
                this::newSolution);

        try {
            // Wait until the solving ends
            solution = solverJob.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }

        logger.info(String.valueOf(solutionManager.explain(solution)));
        dataRepository.update(solution);
    }

    private void newSolution(Schedule schedule) {
        dataRepository.update(schedule);
        String event = "New Update " + LocalDateTime.now();
        timefoldWebsocket.setLatestEvent(event);
    }

    @GET
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Data read() {
        return dataRepository.solution().orElseThrow();
    }
}

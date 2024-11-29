package com.optazen.skillmatch.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optazen.skillmatch.api.Crud;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.api.Sync;
import com.optazen.skillmatch.api.Assignment;
import com.optazen.skillmatch.bootstrap.StartupInitializer;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.persistence.DataRepository;
import com.optazen.skillmatch.service.ScoreAnalysisService;
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
import java.util.stream.Collectors;


@Path("/api")
public class ApiResource {
    public static final Long SINGLETON_SCHEDULE_ID = 1L;
    protected static final Logger logger = LoggerFactory.getLogger(ApiResource.class);

    @Inject
    SolverManager<Schedule, Long> solverManager;
    @Inject
    SolutionManager<Schedule, HardMediumSoftScore> solutionManager;

    @Inject
    TimefoldWebsocket timefoldWebsocket;

    @Inject
    DataRepository dataRepository;

    @Inject
    StartupInitializer startupInitializer;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ScoreAnalysisService scoreAnalysisService;

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
        boolean allSucceeded = true;
        List<Event> unplannedEvents = new ArrayList<>();
        List<Event> addedEvents = new ArrayList<>();
        List<Assignment> addedAssignments = new ArrayList<>();
        List<Map<String, Object>> addedResourcesResponse = new ArrayList<>();

        Crud<Assignment> assignments = sync.getAssignments();
        if (assignments != null) {
            allSucceeded &= assignments.getUpdated().stream().allMatch(assignment -> dataRepository.update(assignment));
            allSucceeded &= assignments.getRemoved().stream().allMatch(assignment -> dataRepository.deleteAssignment(assignment.getEventId()));
            addedAssignments = assignments.getAdded().stream().map(assignment -> dataRepository.addAssignment(assignment)).collect(Collectors.toList());
        }

        Crud<Event> events = sync.getEvents();
        if (events != null) {
            allSucceeded &= events.getUpdated().stream().allMatch(event -> dataRepository.update(event));
            allSucceeded &= events.getRemoved().stream().allMatch(event -> dataRepository.deleteEvent(event.getId()));
            addedEvents = events.getAdded().stream().map(event -> dataRepository.add(event)).toList();
        }

        Crud<Resource> resources = sync.getResources();
        if (resources != null) {
            allSucceeded &= resources.getUpdated().stream().allMatch(resource -> dataRepository.update(resource));
            unplannedEvents = resources.getRemoved().stream().map(resource -> dataRepository.deleteResource(resource.getId())).flatMap(Collection::stream).toList();

            for (Resource resource : resources.getAdded()) {
                Resource addedResource = dataRepository.addResource(resource);
                Map<String, Object> resourceMap = objectMapper.convertValue(addedResource, Map.class);
                if (resource.get$PhantomId() != null) {
                    resourceMap.put("$PhantomId", resource.get$PhantomId());
                }
                addedResourcesResponse.add(resourceMap);
            }
        }

        Crud<Event> unplanned = sync.getUnplanned();
        if (unplanned != null) {
            unplannedEvents = unplanned.getAdded().stream().map(event -> dataRepository.addUnplanned(event)).toList();  
            allSucceeded &= unplanned.getRemoved().stream().allMatch(event -> dataRepository.deleteUnplanned(event.getId()));
        }

        Map<String, Object> jsonResponseObject = new HashMap<>();
        jsonResponseObject.put("success", allSucceeded);
        jsonResponseObject.put("requestId", sync.getRequestId());

        if (!unplannedEvents.isEmpty()) {
            jsonResponseObject.put("unplanned", Collections.singletonMap("rows",
                    unplannedEvents.stream()
                            .map(event -> objectMapper.convertValue(event, Map.class))
                            .collect(Collectors.toList())));
        }

        if (!addedEvents.isEmpty()) {
            jsonResponseObject.put("events", Collections.singletonMap("rows",
                    addedEvents.stream()
                            .map(event -> objectMapper.convertValue(event, Map.class))
                            .collect(Collectors.toList())));
        }

        if (!addedResourcesResponse.isEmpty()) {
            jsonResponseObject.put("resources", Collections.singletonMap("rows", addedResourcesResponse));
        }

        // From the Solver's perspective, it does not matter if we have assignments object separately or not
        // This is just to suppress the validateSyncResponse warning
        if (!addedAssignments.isEmpty()) {
            jsonResponseObject.put("assignments", Collections.singletonMap("rows",
                    addedAssignments.stream()
                            .map(assignment -> {
                                Map<String, Object> assignmentMap = new HashMap<>();
                                assignmentMap.put("$PhantomId", assignment.get$PhantomId());
                                assignmentMap.put("id", assignment.getId());
                                return assignmentMap;
                            })
                            .collect(Collectors.toList())));
        }

        jsonResponseObject.put("scoreAnalysis", scoreAnalysisService.analysis(dataRepository.solution().orElseThrow().getSchedule()));

        return allSucceeded ? Response.ok(jsonResponseObject).build() : Response.serverError().build();
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
        SolverJob<Schedule, Long> solverJob = solverManager.solveBuilder()
                .withProblemId(SINGLETON_SCHEDULE_ID)
                .withProblem(dataRepository.solution().orElseThrow().getSchedule())
                .withBestSolutionConsumer(this::newSolution)
                .withFinalBestSolutionConsumer(this::bestSolution)
                .run();
    }

    @GET
    @Path("/scoreAnalysis")
    public ScoreAnalysis<HardMediumSoftScore> scoreAnalysis() {
        return scoreAnalysisService.analysis(dataRepository.solution().orElseThrow().getSchedule());
    }

    private void newSolution(Schedule schedule) {
        dataRepository.update(schedule);
        timefoldWebsocket.setLatestEvent("New Update " + LocalDateTime.now());
    }

    private void bestSolution(Schedule schedule) {
        dataRepository.update(schedule);
        timefoldWebsocket.setLatestEvent("Finished " + LocalDateTime.now());
    }

    @GET
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Data read() {
        return dataRepository.solution().orElseThrow();
    }
}

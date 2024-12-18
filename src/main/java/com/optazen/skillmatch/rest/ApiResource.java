package com.optazen.skillmatch.rest;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optazen.skillmatch.api.Assignment;
import com.optazen.skillmatch.api.Crud;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.api.Sync;
import com.optazen.skillmatch.bootstrap.StartupInitializer;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.persistence.DataRepository;
import com.optazen.skillmatch.service.ScoreAnalysisService;
import com.optazen.skillmatch.websocket.TimefoldWebsocket;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Path("/api")
public class ApiResource {
    protected static final Logger logger = LoggerFactory.getLogger(ApiResource.class);

    @Inject
    SolverManager<Schedule, UUID> solverManager;

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

    public final static UUID defaultUUID = new UUID(0L, 0L);

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Data update(@QueryParam("scheduleId") UUID tmpScheduleId, Data data) {
        UUID scheduleId = verifyScheduleId(tmpScheduleId);
        return dataRepository.updateWithData(scheduleId, data);
    }

    @POST
    @Path("/sync")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sync(@QueryParam("scheduleId") UUID tmpScheduleId, Sync sync) {
        UUID scheduleId = verifyScheduleId(tmpScheduleId);
        boolean allSucceeded = true;
        List<Event> unplannedEvents = new ArrayList<>();
        List<Event> addedEvents = new ArrayList<>();
        List<Resource> addedResources = new ArrayList<>();
        List<Map<String, Object>> addedResourcesResponse = new ArrayList<>();

        Crud<Event> events = sync.getEvents();
        if (events != null) {
            allSucceeded &= events.getUpdated().stream().allMatch(event -> dataRepository.updateEvent(scheduleId, event));
            allSucceeded &= events.getRemoved().stream().allMatch(event -> dataRepository.deleteEvent(scheduleId, event.getId()));
            addedEvents = events.getAdded().stream().map(event -> dataRepository.addEvent(scheduleId, event)).toList();
        }

        Crud<Resource> resources = sync.getResources();
        if (resources != null) {
            allSucceeded &= resources.getUpdated().stream().allMatch(resource -> dataRepository.updateResource(scheduleId, resource));
            unplannedEvents = resources.getRemoved().stream().map(resource -> dataRepository.deleteResource(scheduleId, resource.getId())).flatMap(Collection::stream).toList();
            addedResources = resources.getAdded().stream().map(resource -> dataRepository.addResource(scheduleId, resource)).toList();
        }

        Crud<Event> unplanned = sync.getUnplanned();
        if (unplanned != null) {
            unplannedEvents = unplanned.getAdded().stream().map(event -> dataRepository.addUnplanned(scheduleId, event)).toList();
            allSucceeded &= unplanned.getRemoved().stream().allMatch(event -> dataRepository.deleteUnplanned(scheduleId, event.getId()));
        }

        Map<String, Object> jsonResponseObject = new HashMap<>();
        jsonResponseObject.put("success", allSucceeded);
        jsonResponseObject.put("requestId", sync.getRequestId());
        jsonResponseObject.put("unplanned", getRows(unplannedEvents));
        jsonResponseObject.put("events", getRows(addedEvents));
        jsonResponseObject.put("resources", getRows(addedResources));

        // From the Solver's perspective, it does not matter if we have assignments object separately or not
        // This is just to suppress the validateSyncResponse warning
        List<Assignment> addedAssignments = sync.getAssignments() != null ? sync.getAssignments().getAdded() : new ArrayList<>();
        if (!addedAssignments.isEmpty()) {
            jsonResponseObject.put("assignments", Collections.singletonMap("rows",
                    addedAssignments.stream()
                            .map(assignment -> {
                                Map<String, Object> assignmentMap = new HashMap<>();
                                assignmentMap.put("$PhantomId", assignment.getPhantomId());
                                assignmentMap.put("id", assignment.getPhantomId());
                                return assignmentMap;
                            })
                            .collect(Collectors.toList())));
        }

        jsonResponseObject.put("scoreAnalysis", scoreAnalysisService.analysis(dataRepository.solution(scheduleId).orElseThrow().getSchedule()));

        return allSucceeded ? Response.ok(jsonResponseObject).build() : Response.serverError().entity("Not all data could be synced successfully").build();
    }

    private Map<String, List<Map>> getRows(List<?> list) {
        return Collections.singletonMap("rows",
                list.stream()
                        .map(event -> objectMapper.convertValue(event, Map.class))
                        .collect(Collectors.toList()));
    }

    private UUID verifyScheduleId(UUID scheduleId) {
        if(scheduleId == null) {
            logger.error("ScheduleId is null");
            return defaultUUID;
        }
        return scheduleId;
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Data reset(@QueryParam("scheduleId") UUID tmpScheduleId) {
        UUID scheduleId = verifyScheduleId(tmpScheduleId);
        Data data = startupInitializer.data();
        dataRepository.updateWithData(scheduleId, data);
        return data;
    }

    @POST
    @Path("/solve")
    @Consumes(MediaType.APPLICATION_JSON)
    public void solve(@QueryParam("scheduleId") UUID tmpScheduleId) {
        UUID scheduleId = verifyScheduleId(tmpScheduleId);
        // Submit the problem to start solving
        solverManager.solveBuilder()
                .withProblemId(scheduleId)
                .withProblem(dataRepository.solution(scheduleId).orElseThrow().getSchedule())
                .withBestSolutionConsumer(schedule -> newSolution(scheduleId, schedule))
                .withFinalBestSolutionConsumer(schedule -> bestSolution(scheduleId, schedule))
                .run();
    }

    @GET
    @Path("/scoreAnalysis")
    public ScoreAnalysis<HardMediumSoftScore> scoreAnalysis(@QueryParam("scheduleId") UUID tmpScheduleId) {
        UUID scheduleId = verifyScheduleId(tmpScheduleId);
        return scoreAnalysisService.analysis(dataRepository.solution(scheduleId).orElseThrow().getSchedule());
    }

    private void newSolution(UUID scheduleId, Schedule schedule) {
        dataRepository.updateWithSchedule(scheduleId, schedule);
        timefoldWebsocket.setLatestEvent(scheduleId, "New Update " + LocalDateTime.now());
    }

    private void bestSolution(UUID scheduleId, Schedule schedule) {
        dataRepository.updateWithSchedule(scheduleId, schedule);
        timefoldWebsocket.setLatestEvent(scheduleId, "Finished " + LocalDateTime.now());
    }

    @GET
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Data read(@QueryParam("scheduleId") UUID tmpScheduleId) {
        UUID scheduleId = verifyScheduleId(tmpScheduleId);
        return dataRepository.solution(scheduleId).orElseThrow();
    }
}

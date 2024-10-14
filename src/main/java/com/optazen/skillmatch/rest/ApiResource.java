package com.optazen.skillmatch.rest;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.bootstrap.StartupInitializer;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.persistence.DataRepository;
import com.optazen.skillmatch.websocket.TimefoldWebsocket;
import io.quarkus.runtime.StartupEvent;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

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

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Data update(Data data) {
        return dataRepository.update(data);
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
        timefoldWebsocket.broadcast(event);
    }

    @GET
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Data read() {
        return dataRepository.solution().orElseThrow();
    }
}

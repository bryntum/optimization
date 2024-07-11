package com.optazen.skillmatch;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.optazen.skillmatch.api.Data;
import com.optazen.skillmatch.bootstrap.StartupInitializer;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.persistence.ScheduleRepository;
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
    ScheduleRepository scheduleRepository;

    @Inject
    StartupInitializer startupInitializer;

    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Schedule update(Data data) {
        return scheduleRepository.update(data.getSchedule());
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Schedule reset() throws URISyntaxException, IOException {
        return startupInitializer.data(new StartupEvent());
    }

    @POST
    @Path("/solve")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Data solve() {
        // Submit the problem to start solving
        Optional<Schedule> schedule = scheduleRepository.solution();
        Schedule solution;
        SolverJob<Schedule, Long> solverJob = solverManager.solve(SINGLETON_SCHEDULE_ID, schedule.orElseThrow());

        try {
            // Wait until the solving ends
            solution = scheduleRepository.update(solverJob.getFinalBestSolution());
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }

        logger.info(String.valueOf(solutionManager.explain(solution)));
        return new Data(solution);
    }

    @GET
    @Path("/read")
    @Produces(MediaType.APPLICATION_JSON)
    public Data read() throws URISyntaxException, IOException {
        return new Data(scheduleRepository.solution().orElseThrow());
    }
}

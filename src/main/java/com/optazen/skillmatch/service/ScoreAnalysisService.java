package com.optazen.skillmatch.service;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import com.optazen.skillmatch.domain.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ScoreAnalysisService {
    @Inject
    SolutionManager<Schedule, HardSoftScore> solutionManager;

    public ScoreAnalysis<HardSoftScore> analysis(Schedule schedule) {
        return solutionManager.analyze(schedule);
    }

}

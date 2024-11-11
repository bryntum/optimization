package com.optazen.skillmatch.service;

import ai.timefold.solver.core.api.score.analysis.ScoreAnalysis;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import com.optazen.skillmatch.domain.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ScoreAnalysisService {
    @Inject
    SolutionManager<Schedule, HardMediumSoftScore> solutionManager;

    public ScoreAnalysis<HardMediumSoftScore> analysis(Schedule schedule) {
        return solutionManager.analyze(schedule);
    }

}

package com.optazen.skillmatch.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.TimeBucket;

import java.util.function.Function;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

public class ScheduleConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                skillMatch(constraintFactory),
                overtime(constraintFactory),
                early(constraintFactory)
        };
    }

    protected Constraint skillMatch(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Event.class)
                .filter(event -> !event.getTimeBucket().getResource().getSkills().containsAll(event.getSkills()))
                .penalize(HardSoftScore.ofHard(1))
                .asConstraint("SkillMatch");
    }

    protected Constraint overtime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Event.class).
                join(TimeBucket.class,
                        Joiners.equal(Event::getTimeBucket, Function.identity()))
                .groupBy((event, timeBucket) -> event.getTimeBucket(), sum((event, timeBucket) -> event.getDuration()))
                .filter((timeBucket, duration) -> duration > timeBucket.getHours())
                .penalize(HardSoftScore.ofHard(1), (timeBucket, duration) -> duration - timeBucket.getHours())
                .asConstraint("Overtime");
    }

    private Constraint early(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Event.class)
                .filter(event -> event.getTimeBucket().getDayIndex() > 0)
                .penalize(HardSoftScore.ofSoft(1), event -> event.getTimeBucket().getDayIndex())
                .asConstraint("Early");
    }


}

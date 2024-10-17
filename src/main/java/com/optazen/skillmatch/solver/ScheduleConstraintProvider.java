package com.optazen.skillmatch.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.TimeBucket;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.sum;

public class ScheduleConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                skillMatch(constraintFactory),
                overlap(constraintFactory),
                overtime(constraintFactory),
                early(constraintFactory)
        };
    }

    protected Constraint skillMatch(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Event.class)
                .filter(event -> !event.getResource().getSkills().containsAll(event.getSkills()))
                .penalize(HardSoftScore.ofHard(1))
                .asConstraint("SkillMatch");
    }

    protected Constraint overlap(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(Event.class,
                        Joiners.equal(Event::getResource),
                        Joiners.overlapping(Event::getStartDate, Event::getEndDate))
                .penalize(HardSoftScore.ofHard(1))
                .asConstraint("Overlap");
    }

    protected Constraint overtime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Event.class)
                .join(ConstraintParameters.class)
                .filter((event, constraintParameters) -> event.getEndDate().isAfter(LocalDateTime.of(event.getStartDate().toLocalDate(), constraintParameters.endTime())))
                .penalize(HardSoftScore.ofHard(1), (event, constraintParameters) -> Math.toIntExact(ChronoUnit.HOURS.between(LocalDateTime.of(event.getStartDate().toLocalDate(), constraintParameters.endTime()), event.getEndDate())))
                .asConstraint("Overtime");
    }

    protected Constraint early(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Event.class)
                .join(ConstraintParameters.class)
                .penalize(HardSoftScore.ofSoft(1), (event, constraintParameters) -> Math.toIntExact(ChronoUnit.HOURS.between(LocalDateTime.of(constraintParameters.startDate(), constraintParameters.startTime()), event.getStartDate())))
                .asConstraint("Early");
    }


}

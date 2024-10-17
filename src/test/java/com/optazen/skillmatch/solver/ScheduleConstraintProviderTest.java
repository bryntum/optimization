package com.optazen.skillmatch.solver;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.optazen.skillmatch.domain.Event;
import com.optazen.skillmatch.domain.Resource;
import com.optazen.skillmatch.domain.Schedule;
import com.optazen.skillmatch.domain.TimeBucket;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
class ScheduleConstraintProviderTest {
    @Inject
    ConstraintVerifier<ScheduleConstraintProvider, Schedule> constraintVerifier;

    @Test
    void skillMatch() {
        Event event = new Event();
        event.setSkills(List.of(1,2));

        Resource resource = new Resource();
        resource.setSkills(List.of(1, 2, 3));

        Resource resource2 = new Resource();
        resource2.setSkills(List.of(2, 3));

        Resource resource3 = new Resource();
        resource3.setSkills(List.of(3,4));

        Resource resource4 = new Resource();
        resource4.setSkills(List.of(1));

//        timeBucket.setResource(resource);
//        constraintVerifier.verifyThat(ScheduleConstraintProvider::skillMatch)
//                .given(event, timeBucket)
//                .penalizesBy(0);
//
//        timeBucket.setResource(resource2);
//        constraintVerifier.verifyThat(ScheduleConstraintProvider::skillMatch)
//                .given(event, timeBucket)
//                .penalizesBy(1);
//
//        timeBucket.setResource(resource3);
//        constraintVerifier.verifyThat(ScheduleConstraintProvider::skillMatch)
//                .given(event, timeBucket)
//                .penalizesBy(1);
//
//        timeBucket.setResource(resource4);
//        constraintVerifier.verifyThat(ScheduleConstraintProvider::skillMatch)
//                .given(event, timeBucket)
//                .penalizesBy(1);
    }

    @Test
    void overtime() {
        Event event1 = new Event();
        event1.setDuration(5);
        Event event2 = new Event();
        event2.setDuration(5);

        TimeBucket timeBucket = new TimeBucket();
        timeBucket.setHours(8);

        TimeBucket timeBucket2 = new TimeBucket();
        timeBucket2.setHours(8);

//        // place only event1 in the first time bucket
//        event1.setTimeBucket(timeBucket);
//        constraintVerifier.verifyThat(ScheduleConstraintProvider::overtime)
//                .given(event1, event2, timeBucket, timeBucket2)
//                .penalizesBy(0);
//
//        // place the second event in the other time buckets
//        event2.setTimeBucket(timeBucket2);
//        constraintVerifier.verifyThat(ScheduleConstraintProvider::overtime)
//                .given(event1, event2, timeBucket, timeBucket2)
//                .penalizesBy(0);
//
//        // overtime of 2h after placing both events in the same time bucket of 8h
//        event2.setTimeBucket(timeBucket);
//        constraintVerifier.verifyThat(ScheduleConstraintProvider::overtime)
//                .given(event1, event2, timeBucket, timeBucket2)
//                .penalizesBy(2);

    }
}
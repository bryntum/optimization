package com.optazen.skillmatch.solver;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record ConstraintParameters(LocalDate startDate, LocalDate endDate, Set<Integer> workingDays, LocalTime startTime, LocalTime endTime) {
}
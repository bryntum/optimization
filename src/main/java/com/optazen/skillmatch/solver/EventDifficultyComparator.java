package com.optazen.skillmatch.solver;

import com.optazen.skillmatch.domain.Event;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class EventDifficultyComparator implements Comparator<Event> {
    public int compare(Event a, Event b) {
        return new CompareToBuilder()
                .append(a.getDuration(), b.getDuration())
                .append(a.getId(), b.getId())
                .toComparison();
    }
}

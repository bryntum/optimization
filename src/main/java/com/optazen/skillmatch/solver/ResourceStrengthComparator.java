package com.optazen.skillmatch.solver;

import com.optazen.skillmatch.domain.Resource;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

public class ResourceStrengthComparator implements Comparator<Resource> {
    public int compare(Resource a, Resource b) {
        return new CompareToBuilder()
                .append(a.getSkills().size(), b.getSkills().size())
                .append(a.getId(), b.getId())
                .toComparison();
    }
}

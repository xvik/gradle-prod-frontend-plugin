package ru.vyarus.gradle.frontend.core.model;

import ru.vyarus.gradle.frontend.core.info.Stat;
import ru.vyarus.gradle.frontend.core.info.resources.OptimizedEntityInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 02.02.2023
 */
public abstract class OptimizedEntity implements OptimizedEntityInfo {

    private final List<String> changes = new ArrayList<>();
    private final Map<Stat, Long> stats = new HashMap<>();
    private boolean ignored;
    private String ignoreReason;

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public String getIgnoreReason() {
        return ignoreReason;
    }

    protected void ignore(final String reason) {
        ignored = true;
        ignoreReason = reason;
    }

    @Override
    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    @Override
    public List<String> getChanges() {
        return changes;
    }

    @Override
    public Map<Stat, Long> getStats() {
        return stats;
    }

    protected void recordChange(final String change) {
        changes.add(change);
    }

    protected void recordStat(final Stat stat, final Number value) {
        if (stats.containsKey(stat)) {
            throw new IllegalStateException("Stat value override: " + stat);
        }
        stats.put(stat, value.longValue());
    }
}

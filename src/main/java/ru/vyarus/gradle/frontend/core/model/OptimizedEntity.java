package ru.vyarus.gradle.frontend.core.model;

import ru.vyarus.gradle.frontend.core.info.SizeType;
import ru.vyarus.gradle.frontend.core.info.resources.OptimizedEntityInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for processed resources: html, js, css, and sub resources (css relatives).
 *
 * @author Vyacheslav Rusakov
 * @since 02.02.2023
 */
public abstract class OptimizedEntity implements OptimizedEntityInfo {

    /**
     * List of resource changes (tracked manually for audit).
     */
    private final List<String> changes = new ArrayList<>();
    /**
     * Resource size stats (original, minified, gzipped).
     */
    private final Map<SizeType, Long> stats = new HashMap<>();
    /**
     * True if resource ignored (e.g. local file not found or can't load remote resource).
     */
    private boolean ignored;
    /**
     * Ignore reason (for audit).
     */
    private String ignoreReason;

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public String getIgnoreReason() {
        return ignoreReason;
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
    public Map<SizeType, Long> getStats() {
        return stats;
    }

    /**
     * Mark resource as ignored.
     *
     * @param reason ignore reason
     */
    protected void ignore(final String reason) {
        ignored = true;
        ignoreReason = reason;
    }

    /**
     * Store applied resource change for audit.
     *
     * @param change change description for audit
     */
    protected void recordChange(final String change) {
        changes.add(change);
    }

    /**
     * Record resource size for audit. As file changes during optimization it would be impossible to recover
     * original size without this.
     *
     * @param type  size type
     * @param value size value
     */
    protected void recordSize(final SizeType type, final Number value) {
        if (stats.containsKey(type)) {
            throw new IllegalStateException("Size value override: " + type);
        }
        stats.put(type, value.longValue());
    }
}

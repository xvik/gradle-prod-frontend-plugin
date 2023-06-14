package ru.vyarus.gradle.frontend.core.info.resources;

import ru.vyarus.gradle.frontend.core.info.Stat;

import java.util.List;
import java.util.Map;

/**
 * Common attribute for optimized entities (html pages, css and js, sub css resources).
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface OptimizedEntityInfo {

    /**
     * @return true if resource was ignored (e.g. it was not wound locally)
     */
    boolean isIgnored();

    /**
     * @return ignorance reason
     * @see #isIgnored()
     */
    String getIgnoreReason();

    /**
     * @return true if resource was modified
     */
    boolean hasChanges();

    /**
     * @return list of applied modifications
     */
    List<String> getChanges();

    /**
     * @return recorded entity stats (original size - optimized size)
     */
    Map<Stat, Long> getStats();
}

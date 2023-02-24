package ru.vyarus.gradle.frontend.core.info;

import ru.vyarus.gradle.frontend.core.stat.Stat;

import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface ResourceInfo {

    boolean isIgnored();

    String getIgnoreReason();

    boolean hasChanges();

    List<String> getChanges();

    Map<Stat, Long> getStats();
}

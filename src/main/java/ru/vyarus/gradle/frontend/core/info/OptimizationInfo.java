package ru.vyarus.gradle.frontend.core.info;

import ru.vyarus.gradle.frontend.core.OptimizationFlow;
import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo;

import java.util.List;

/**
 * Performed optimization info.
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface OptimizationInfo {

    /**
     * @return used optimization settings
     */
    OptimizationFlow.Settings getSettings();

    /**
     * @return optimized html pages
     */
    List<? extends HtmlInfo> getHtmls();

    /**
     * Print optimization stats to console.
     */
    void printStats();
}

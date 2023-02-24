package ru.vyarus.gradle.frontend.core.info;

import ru.vyarus.gradle.frontend.core.OptimizationFlow;

import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface OptimizationInfo {
    OptimizationFlow.Settings getSettings();

    List<? extends HtmlInfo> getHtmls();

    void printStats();
}

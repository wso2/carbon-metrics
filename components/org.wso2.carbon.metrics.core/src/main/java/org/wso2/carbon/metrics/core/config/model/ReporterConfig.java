/*
 * Copyright 2016 WSO2 Inc. (http://wso2.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.metrics.core.config.model;

import com.codahale.metrics.MetricFilter;
import org.wso2.carbon.metrics.core.reporter.ReporterBuildException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Base class for configuring metric reporters.
 */
public abstract class ReporterConfig {

    private String name;

    private boolean enabled = false;

    private boolean useRegexFilters = false;

    private Set<String> includes = new HashSet<>();

    private Set<String> excludes = new HashSet<>();

    private static final DefaultStringMatchingStrategy defaultStringMatchingStrategy =
            new DefaultStringMatchingStrategy();

    private static final RegexStringMatchingStrategy regexStringMatchingStrategy = new RegexStringMatchingStrategy();

    private static final Map<String, Pattern> patternMap = new ConcurrentHashMap<>();

    public ReporterConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUseRegexFilters() {
        return useRegexFilters;
    }

    public void setUseRegexFilters(boolean useRegexFilters) {
        this.useRegexFilters = useRegexFilters;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReporterConfig that = (ReporterConfig) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Gets a {@link MetricFilter} that specifically includes and excludes configured metrics. This method needs the
     * existing {@link MetricFilter} used to filter the disabled metrics. The includes and excludes will be checked
     * only for enabled metrics.
     *
     * @param enabledFilter The existing {@link MetricFilter} to filter disabled metrics.
     * @return the filter for selecting metrics based on the configured excludes/includes.
     * @throws ReporterBuildException if the pattern compilation failed for regular expressions.
     */
    protected MetricFilter getFilter(MetricFilter enabledFilter) throws ReporterBuildException {
        if (includes.isEmpty() && excludes.isEmpty()) {
            return enabledFilter;
        }

        final StringMatchingStrategy stringMatchingStrategy;

        if (useRegexFilters) {
            stringMatchingStrategy = regexStringMatchingStrategy;
            compileAllRegex(getIncludes());
            compileAllRegex(getExcludes());
        } else {
            stringMatchingStrategy = defaultStringMatchingStrategy;
        }

        return (name, metric) -> {
            // Include the metric if its name is not excluded and its name is included
            // Where, by default, with no includes setting, all names are included.
            return enabledFilter.matches(name, metric) && !stringMatchingStrategy.containsMatch(getExcludes(), name) &&
                    (getIncludes().isEmpty() || stringMatchingStrategy.containsMatch(getIncludes(), name));
        };
    }

    private interface StringMatchingStrategy {
        boolean containsMatch(Set<String> matchExpressions, String metricName);
    }

    private static class DefaultStringMatchingStrategy implements StringMatchingStrategy {
        @Override
        public boolean containsMatch(Set<String> matchExpressions, String metricName) {
            return matchExpressions.contains(metricName);
        }
    }

    private static class RegexStringMatchingStrategy implements StringMatchingStrategy {

        @Override
        public boolean containsMatch(Set<String> matchExpressions, String metricName) {
            for (String regex : matchExpressions) {
                Pattern pattern = patternMap.get(regex);
                if (pattern != null && pattern.matcher(metricName).matches()) {
                    // match a single value and return
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Compile all regular expressions and keep the compiled {@link Pattern} in a map.
     *
     * @param matchExpressions The regular expressions to compile
     * @throws ReporterBuildException if the compile failed
     */
    private void compileAllRegex(Set<String> matchExpressions) throws ReporterBuildException {
        for (String regex : matchExpressions) {
            Pattern pattern = patternMap.get(regex);
            if (pattern == null) {
                try {
                    pattern = Pattern.compile(regex);
                    patternMap.put(regex, pattern);
                } catch (PatternSyntaxException e) {
                    throw new ReporterBuildException(String.format("Failed to compile regex \"%s\" used in the " +
                            "\"%s\" reporter", regex, name), e);
                }
            }
        }
    }
}

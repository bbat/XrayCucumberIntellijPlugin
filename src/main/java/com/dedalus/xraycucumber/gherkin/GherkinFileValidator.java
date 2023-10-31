package com.dedalus.xraycucumber.gherkin;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.dedalus.xraycucumber.exceptions.GherkinParseException;

import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;

public class GherkinFileValidator {

    public void validate(final Feature feature) {
        Objects.requireNonNull(feature, "The feature file cannot be null");
        checkDocumentContainsScenarios(feature);
        checkScenarioNamesUnicity(feature.getChildren());
    }

    private void checkScenarioNamesUnicity(final List<FeatureChild> scenarioList) {
        var seenScenarioNames = new HashSet<String>();

        for (FeatureChild featureChild : scenarioList) {
            featureChild.getScenario().ifPresent(scenario -> {
                if (scenario.getName().isEmpty() || seenScenarioNames.contains(scenario.getName())) {
                    throw new GherkinParseException("Null or duplicated scenario name found: " + scenario.getName());
                } else {
                    seenScenarioNames.add(scenario.getName());
                }
            });
        }
    }

    private void checkDocumentContainsScenarios(final Feature feature) {
        if (feature.getChildren().isEmpty()) {
            throw new GherkinParseException("This feature file doesn't contain a scenario");
        }
    }
}

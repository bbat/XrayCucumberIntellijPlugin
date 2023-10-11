package com.dedalus.xraycucumber.gherkin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.dedalus.xraycucumber.exceptions.GherkinParseException;
import com.intellij.openapi.vfs.VirtualFile;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.Tag;

/**
 * A parser for Gherkin feature files that extracts scenarios and their associated tags.
 * <p>
 * This parser relies on the Cucumber Gherkin parser and allows users to retrieve
 * scenarios along with their tags from a provided Gherkin feature file, represented
 * as a {@link VirtualFile}. The parsed scenarios and tags are provided as a {@link Map}
 * where each scenario name (a {@link String}) is associated with a {@link List} of tag names.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * GherkinFileParser parser = new GherkinFileParser();
 * Map<String, List<String>> scenariosWithTags = parser.getScenariosAndTags(featureFile);
 * }
 * </pre>
 * Note: Scenarios with duplicate names within a single feature file are not supported
 * and will result in an {@link IllegalStateException} being thrown.
 *
 */
public class GherkinFileParser {

    /**
     * Parses the provided Gherkin feature file and extracts all scenarios and their associated tags.
     *
     * @param featureFile the {@link VirtualFile} pointing to the Gherkin feature file to be parsed.
     * @return a {@link Map} where each scenario name is associated with a list of its tag names.
     * @throws GherkinParseException if the Gherkin feature file is not correctly formatted,
     *                               contains no scenarios, or contains scenarios with duplicate names.
     */
    public Map<String, List<String>> getScenariosAndTags(VirtualFile featureFile) {
        Objects.requireNonNull(featureFile, "The feature file cannot be null");

        var featureFilePath = featureFile.getPath();
        var scenariosWithTags = new HashMap<String, List<String>>();
        var seenScenarioNames = new HashSet<String>();

        try {
            Optional<GherkinDocument> gherkinDocument = this.parse(featureFilePath);

            if (gherkinDocument.isEmpty()) {
                throw new GherkinParseException("Error reading Gherkin file: " + featureFilePath);
            }

            gherkinDocument.get().getFeature().ifPresent(feature -> processFeatureChildren(feature.getChildren(), seenScenarioNames, scenariosWithTags));

            return scenariosWithTags;

        } catch (IOException e) {
            throw new GherkinParseException("Error reading Gherkin file: " + featureFilePath, e);
        }
    }

    private void processScenario(Scenario scenario, Set<String> seenScenarioNames, Map<String, List<String>> scenariosWithTags) {
        String scenarioName = scenario.getName();
        Objects.requireNonNull(scenarioName, "Scenario name cannot be null");

        if (seenScenarioNames.contains(scenarioName)) {
            throw new GherkinParseException("Duplicate scenario name found: " + scenarioName);
        }

        seenScenarioNames.add(scenarioName);
        List<String> tags = scenario.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
        scenariosWithTags.put(scenario.getName(), tags);
    }

    private void processFeatureChildren(List<FeatureChild> featureChildren, Set<String> seenScenarioNames, Map<String, List<String>> scenariosWithTags) {
        if (featureChildren.isEmpty()) {
            throw new GherkinParseException("There are no scenario in the Feature file");
        }
        for (FeatureChild featureChild : featureChildren) {
            featureChild.getScenario().ifPresent(scenario -> processScenario(scenario, seenScenarioNames, scenariosWithTags));
        }
    }

    /**
     * Parses the Gherkin feature file specified by its file path and returns the parsed
     * {@link GherkinDocument} wrapped in an {@link Optional}.
     *
     * @param featureFilePath the file path of the Gherkin feature file to be parsed.
     * @return an {@link Optional} containing the parsed {@link GherkinDocument},
     *         or {@link Optional#empty()} if parsing fails.
     * @throws IOException if an I/O exception occurs while reading the feature file.
     */
    private Optional<GherkinDocument> parse(String featureFilePath) throws IOException {
        Objects.requireNonNull(featureFilePath, "The feature file path cannot be null");

        var featureContent = Files.readString(Paths.get(featureFilePath));
        var envelope = Envelope.of(
                new Source(featureFilePath,
                        featureContent,
                        io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN
                )
        );

        return GherkinParser.builder()
                .includeSource(false)
                .includePickles(false)
                .build()
                .parse(envelope)
                .findFirst()
                .flatMap(Envelope::getGherkinDocument);
    }
}

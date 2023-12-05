package com.dedalus.xraycucumber.gherkin;

import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dedalus.xraycucumber.exceptions.GherkinParseException;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.Tag;

public class GherkinFileParser {

    /**
     * Extracts all scenarios and their associated tags from a feature file
     *
     * @param featurefilePath the {@link String} path to the gherkin file.
     * @return a {@link Map} where each scenario name is associated with a list of its tag names.
     * @throws GherkinParseException if the Gherkin feature file is not correctly formatted,
     *                               contains no scenarios, or contains scenarios with duplicate names.
     */
    public Map<String, List<String>> getScenariosAndTags(String featurefilePath) throws IOException {
        Objects.requireNonNull(featurefilePath, "The feature file cannot be null");

        Feature feature = this.parse(featurefilePath);
        new GherkinFileValidator().validate(feature);

        Map<String, List<String>> scenariosWithTags = new HashMap<>();

        for (FeatureChild featureChild : feature.getChildren()) {
            featureChild.getScenario().ifPresent(scenario -> {
                List<String> tags = scenario.getTags().stream().map(Tag::getName).collect(Collectors.toList());
                scenariosWithTags.put(scenario.getName(), tags);
            });
        }

        return scenariosWithTags;
    }

    public Feature parse(String featureFilePath) throws IOException {
        Objects.requireNonNull(featureFilePath, "The feature file path cannot be null");

        var featureContent = Files.readString(Paths.get(featureFilePath));
        var envelope = Envelope.of(new Source(featureFilePath, featureContent, TEXT_X_CUCUMBER_GHERKIN_PLAIN));

        Optional<GherkinDocument> gherkinDocument = GherkinParser
                .builder()
                .includeSource(false)
                .includePickles(false)
                .build().parse(envelope)
                .findFirst()
                .flatMap(Envelope::getGherkinDocument);

        if (gherkinDocument.isPresent()) {
            if(gherkinDocument.get().getFeature().isPresent()) {
                return gherkinDocument.get().getFeature().get();
            } else {
                throw new GherkinParseException("this document is not a Gherkin feature file");
            }
        } else {
            throw new GherkinParseException("this document is not a Gherkin feature file");
        }
    }
}

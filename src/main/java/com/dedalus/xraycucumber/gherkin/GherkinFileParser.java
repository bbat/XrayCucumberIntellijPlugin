package com.dedalus.xraycucumber.gherkin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.intellij.openapi.vfs.VirtualFile;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.Tag;

public class GherkinFileParser {

    public Map<String, List<String>> getScenariosAndTags(VirtualFile featureFile) {
        String featureFilePath = featureFile.getPath();
        Map<String, List<String>> scenariosWithTags = new HashMap<>();
        Set<String> seenScenarioNames = new HashSet<>();  // Un ensemble pour garder une trace des noms de scénarios vus.

        try {
            Optional<GherkinDocument> gherkinDocument = this.parse(featureFilePath);
            if (gherkinDocument.isEmpty()) {
                throw new IllegalStateException("Gherkin Feature Not Ready, probably because it is not correctly formatted");
            } else {
                gherkinDocument.get().getFeature().ifPresent(feature -> {
                    if (feature.getChildren().size() == 0) {
                        throw new IllegalStateException("There are no scenario in the Feature file");
                    }
                    for (FeatureChild featureChild : feature.getChildren()) {
                        Optional<Scenario> scenario = featureChild.getScenario();
                        if (scenario.isPresent()) {
                            String scenarioName = scenario.get().getName();
                            if (seenScenarioNames.contains(scenarioName)) {
                                throw new IllegalStateException("Duplicate scenario name found: " + scenarioName);
                            }
                            seenScenarioNames.add(scenarioName);
                            List<String> tags = scenario.get().getTags().stream()
                                    .map(Tag::getName)
                                    .collect(Collectors.toList());
                            scenariosWithTags.put(scenario.get().getName(), tags);
                        }
                    }
                });
                return scenariosWithTags;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<GherkinDocument> parse(String featureFilePath) throws IOException {
        String featureContent = Files.readString(Paths.get(featureFilePath));
        Envelope envelope = Envelope.of(
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

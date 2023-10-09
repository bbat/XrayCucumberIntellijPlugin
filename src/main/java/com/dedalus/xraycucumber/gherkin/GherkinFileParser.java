package com.dedalus.xraycucumber.gherkin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        try {
            Optional<GherkinDocument> gherkinDocument = this.parse(featureFilePath);
            if (gherkinDocument.isEmpty()) {
                throw new IllegalStateException("Gherkin document is not ready");
            } else {
                gherkinDocument.get().getFeature().ifPresent(feature -> {
                    for (FeatureChild featureChild : feature.getChildren()) {
                        Optional<Scenario> scenario = featureChild.getScenario();
                        if (scenario.isPresent()) {
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

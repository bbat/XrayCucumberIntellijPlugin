package com.dedalus.xraycucumber.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import com.dedalus.xraycucumber.model.ServiceParameters;
import com.dedalus.xraycucumber.service.XrayCucumberService;
import com.dedalus.xraycucumber.ui.utils.NotificationUtils;
import com.dedalus.xraycucumber.ui.utils.ServiceParametersUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Source;
import io.cucumber.messages.types.Tag;

public class UploadTestToJira extends AnAction {

    public static final String TITLE = "Uploading Cucumber Test to Jira";

    public static Map<String, List<String>> getScenariosAndTags(GherkinDocument gherkinDocument) {
        Map<String, List<String>> scenariosWithTags = new HashMap<>();

        gherkinDocument.getFeature().ifPresent(feature -> {
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

    @Override public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }

    private static Map<String, String> createJiraIdMapping(JsonArray issues) {
        Map<String, String> scenarioToJiraIdMap = new HashMap<>();

        for (JsonElement issueElement : issues) {
            JsonObject issueObject = issueElement.getAsJsonObject();

            String scenarioName = issueObject.get("summary").getAsString();
            String jiraId = issueObject.get("key").getAsString();

            scenarioToJiraIdMap.put(scenarioName, jiraId);
        }

        return scenarioToJiraIdMap;
    }

    private static void saveGherkinFileBeforeUpdate(final VirtualFile featureFile) throws IOException {
        Path originalPath = Paths.get(featureFile.getPath());
        String originalNameWithoutExtension = originalPath.getFileName().toString().replace(".feature", "");
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String backupFileName = originalNameWithoutExtension + "_Backup-" + timeStamp + ".feature";
        Path backupPath = originalPath.getParent().resolve(backupFileName);
        Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    void updateFeatureFileWithJiraId(VirtualFile featureFile, JsonArray issues) throws IOException {
        saveGherkinFileBeforeUpdate(featureFile);

        Optional<GherkinDocument> gherkinDocument = parseFeatureFile(featureFile.getPath());
        if (gherkinDocument.isPresent()) {
            Map<String, String> jiraIdMap = createJiraIdMapping(issues);
            Map<String, List<String>> scenariosAndTags = getScenariosAndTags(gherkinDocument.get());

            Path featurePath = Paths.get(featureFile.getPath());
            List<String> featureLines = Files.readAllLines(featurePath);

            StringBuilder updatedFeatureContent = new StringBuilder();

            for (String line : featureLines) {
                if (line.trim().toLowerCase().startsWith("scenario:")) {
                    String scenarioName = line.trim().substring(9).trim();

                    String jiraId = jiraIdMap.get(scenarioName);
                    if (jiraId != null && (scenariosAndTags.get(scenarioName) == null || !scenariosAndTags.get(scenarioName).contains("@" + jiraId))) {
                        updatedFeatureContent.append("@").append(jiraId).append("\n");
                    }
                }
                updatedFeatureContent.append(line).append("\n");
            }

            Files.write(featurePath, updatedFeatureContent.toString().getBytes());
        } else {
            throw new IllegalStateException("Gherkin document is not ready");
        }

    }

    public Optional<GherkinDocument> parseFeatureFile(String featureFilePath) throws IOException {
        String featureContent = Files.readString(Paths.get(featureFilePath));
        Envelope envelope = Envelope.of(new Source("example.feature", featureContent, io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN));

        return GherkinParser.builder()
                .includeSource(false)
                .includePickles(false)
                .build()
                .parse(envelope)
                .findFirst()
                .flatMap(Envelope::getGherkinDocument);
    }


    @Override
    public void update(AnActionEvent event) {
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean visible = file != null && file.getName().endsWith(".feature");
        event.getPresentation().setEnabledAndVisible(visible);
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent event) {
        final Project project = event.getProject();

        FileDocumentManager.getInstance().saveAllDocuments();
        VirtualFile featureFile = event.getData(CommonDataKeys.VIRTUAL_FILE);

        if (featureFile == null) {
            NotificationUtils.notifyError(project, "this action requires a valid feature file");
        }

        VirtualFile serviceParametersFile = featureFile.findFileByRelativePath("../" + ServiceParametersUtils.XRAY_CUCUMBER_JSON);
        if (serviceParametersFile == null) {
            NotificationUtils.notifyError(project, "this action requires a valid " + ServiceParametersUtils.XRAY_CUCUMBER_JSON
                    + " file located in same directory as feature file");
            return;
        }

        ServiceParameters serviceParameters;
        try {
            serviceParameters = ServiceParametersUtils.prepareServiceParameters(project, serviceParametersFile);
        } catch (IOException exception) {
            NotificationUtils.notifyError(project, exception.getMessage());
            return;
        }

        if (serviceParameters == null) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, TITLE) {

            public void run(@NotNull ProgressIndicator progressIndicator) {
                XrayCucumberService xrayCucumberService = new XrayCucumberService(HttpClients.createDefault(), serviceParameters);
                ProgressIndicatorAdapter progressReporter = new ProgressIndicatorAdapter(progressIndicator, project);

                JsonArray issues = xrayCucumberService.uploadXrayCucumberTest(Paths.get(featureFile.getPath()), progressReporter);
                IssueDetailsDisplayer issueDetailsDisplayer = new IssueDetailsDisplayer(project);
                issueDetailsDisplayer.showInPopup(issues);

                progressReporter.reportProgress("Update Feature file with Xray test case Id", 100);
                try {
                    updateFeatureFileWithJiraId(featureFile, issues);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
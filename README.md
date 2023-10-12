# XrayCucumberIntellijPlugin
Intellij Plugin for managing synchronization between Cucumber Features and Xray

Goal
-	upload a Cucumber feature file to Jira from Intellij : this will create or update a Xray test case in Jira for each Scenario of the feature.
-	update the feature file by adding on top of each scenario a tag that represent the Xray Test Case Id.

Usage: 
-	To generate the plugin: run the .\gradlew.bat buildPlugin command from the project root. This will produce a zip file in the build/distribution folder.
-	Install the plugin : in Intellij Settings / plugin / install plugin from disk and chose the zip file.
-	To use the plugin, open a feature file in IntelliJ, right click in it and chose “Upload the feature file to Xray”

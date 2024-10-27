# Build Variant Selector
### Description
Android Studio Plugin that provides a dialog for selecting the build variant for the active run configuration. The variant is selected by choosing a build type and a build flavor for every             dimension of the active run configuration module.

### Download
https://plugins.jetbrains.com/plugin/17550-build-variant-selector

### Building
Run the buildPlugin gradle task.

### Publishing
To publish the plugin, add ```ORG_GRADLE_PROJECT_intellijPlatformPublishingToken=<your-token>``` to the environment variables of the publishPlugin gradle task.

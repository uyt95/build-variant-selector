package me.uyt.build.variant.selector

import com.android.tools.idea.gradle.project.model.AndroidModuleModel
import com.android.tools.idea.gradle.variant.view.BuildVariantUpdater
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.lombok.utils.capitalize

class BuildVariantSelectorAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        try {
            val project = event.project ?: return
            val module = getRunConfigurationModule(project) ?: return
            val flavors = parseFlavors(module)
            val buildTypes = parseBuildTypes(module)

            val shouldSave =
                BuildVariantDialog(module.androidProject.flavorDimensions, flavors, buildTypes).showAndGet()
            if (shouldSave) {
                val selectedVariant = getSelectedVariant(
                    module.androidProject.flavorDimensions,
                    flavors,
                    buildTypes
                )

                if (module.selectedVariantName != selectedVariant) {
                    BuildVariantUpdater.getInstance(project)
                        .updateSelectedBuildVariant(project, module.moduleName, selectedVariant)
                }
            }
        } catch (t: Throwable) {
            showNotification(event, t.toString())
        }
    }

    private fun getRunConfigurationModule(project: Project): AndroidModuleModel? {
        val runConfiguration =
            RunManager.getInstance(project).selectedConfiguration?.configuration as? ModuleBasedConfiguration<*, *>
                ?: return null
        return runConfiguration.configurationModule.module?.let { AndroidModuleModel.get(it) }
    }

    private fun parseFlavors(module: AndroidModuleModel): Map<String, List<SelectOption>> {
        val flavors = mutableMapOf<String, MutableList<SelectOption>>()
        module.androidProject.flavorDimensions.forEach {
            flavors[it] = mutableListOf()
        }
        module.androidProject.productFlavors.forEach { flavorContainer ->
            val flavor = flavorContainer.productFlavor
            val dimension = flavor.dimension ?: return@forEach
            val selected = module.selectedVariant.productFlavors.contains(flavor.name)
            flavors[dimension]?.add(SelectOption(flavor.name, selected))
        }
        return flavors
    }

    private fun parseBuildTypes(module: AndroidModuleModel): List<SelectOption> {
        return module.buildTypes.map { SelectOption(it, it == module.selectedVariant.buildType) }
    }

    private fun getSelectedVariant(
        flavorDimensions: Collection<String>,
        flavors: Map<String, List<SelectOption>>,
        buildTypes: List<SelectOption>
    ): String {
        val selectedVariantBuilder = StringBuilder()
        flavorDimensions.forEach { dimension ->
            flavors[dimension]
                ?.find { flavor -> flavor.isSelected }
                ?.let { flavor ->
                    var selectedFlavor = flavor.name
                    if (selectedVariantBuilder.isNotEmpty()) {
                        selectedFlavor = selectedFlavor.capitalize()
                    }
                    selectedVariantBuilder.append(selectedFlavor)
                }
        }
        buildTypes.find { buildType -> buildType.isSelected }?.let { buildType ->
            var selectedBuildType = buildType.name
            if (selectedVariantBuilder.isNotEmpty()) {
                selectedBuildType = selectedBuildType.capitalize()
            }
            selectedVariantBuilder.append(selectedBuildType)
        }
        return selectedVariantBuilder.toString()
    }

    private fun showNotification(event: AnActionEvent, content: String) {
        NotificationGroup.create(
            "build-variant-selector",
            NotificationDisplayType.BALLOON,
            true,
            null,
            null,
            null,
            null
        )
            .createNotification(
                "Build variant selector - error:",
                content,
                NotificationType.ERROR,
                null
            )
            .notify(event.project)
    }
}
package me.uyt.build.variant.selector

import com.android.tools.idea.gradle.project.model.GradleAndroidModel
import com.android.tools.idea.gradle.variant.view.BuildVariantUpdater
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.lombok.utils.capitalize

class BuildVariantSelectorAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        try {
            val project = event.project ?: return
            val pair = getRunConfigurationModuleAndModel(project) ?: return
            val module = pair.first
            val model = pair.second
            val flavors = parseFlavors(model)
            val buildTypes = parseBuildTypes(model)

            val shouldSave =
                BuildVariantDialog(model.androidProject.flavorDimensions, flavors, buildTypes).showAndGet()
            if (shouldSave) {
                val selectedVariant = getSelectedVariant(
                    model.androidProject.flavorDimensions,
                    flavors,
                    buildTypes
                )

                if (model.selectedVariantName != selectedVariant) {
                    BuildVariantUpdater.getInstance(project).updateSelectedBuildVariant(module, selectedVariant)
                }
            }
        } catch (t: Throwable) {
            showNotification(event, t.toString())
        }
    }

    private fun getRunConfigurationModuleAndModel(project: Project): Pair<com.intellij.openapi.module.Module, GradleAndroidModel>? {
        val runConfiguration =
            RunManager.getInstance(project).selectedConfiguration?.configuration as? ModuleBasedConfiguration<*, *>
                ?: return null
        val module = runConfiguration.configurationModule.module ?: return null
        val model = GradleAndroidModel.get(module) ?: return null
        return Pair(module, model)
    }

    private fun parseFlavors(module: GradleAndroidModel): Map<String, List<SelectOption>> {
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

    private fun parseBuildTypes(module: GradleAndroidModel): List<SelectOption> {
        return module.androidProject.buildTypes.map {
            SelectOption(
                it.buildType.name,
                it.buildType.name == module.selectedVariant.buildType
            )
        }
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
        NotificationGroupManager.getInstance()
            .getNotificationGroup("error")
            .createNotification("Build variant selector - error:", content, NotificationType.ERROR)
            .notify(event.project)
    }
}
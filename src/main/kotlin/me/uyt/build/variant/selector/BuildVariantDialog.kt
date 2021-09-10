package me.uyt.build.variant.selector

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.RadioButton
import org.jetbrains.kotlin.lombok.utils.capitalize
import java.awt.Font
import java.awt.GridLayout
import javax.swing.*

class BuildVariantDialog(
    private val flavorDimensions: Collection<String>,
    private val flavors: Map<String, List<SelectOption>>,
    private val buildTypes: List<SelectOption>
) : DialogWrapper(true) {
    init {
        title = "Build Variant Selector"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridLayout(1, flavorDimensions.size + 1, 8, 0))

        flavorDimensions.sorted().forEach { dimension ->
            flavors[dimension]?.let { options ->
                val dimensionPanel = createSelectPanel(dimension, options)
                panel.add(dimensionPanel)
            }
        }

        val buildTypePanel = createSelectPanel("Build Type", buildTypes)
        panel.add(buildTypePanel)

        return panel
    }

    private fun createSelectPanel(label: String, options: List<SelectOption>): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        panel.add(createLabel(label))

        val buttonGroup = ButtonGroup()
        options.forEach { option ->
            if (panel.componentCount > 0) {
                panel.add(Box.createVerticalStrut(4))
            }

            val radioButton = createRadioButton(option)
            buttonGroup.add(radioButton)
            panel.add(radioButton)
        }

        return panel
    }

    private fun createLabel(text: String): JLabel {
        return JLabel(text.capitalize()).apply {
            font = font.deriveFont(font.style.or(Font.BOLD))
        }
    }

    private fun createRadioButton(option: SelectOption): JRadioButton {
        return RadioButton(option.name).apply {
            isSelected = option.isSelected
            addChangeListener {
                option.isSelected = isSelected
            }
        }
    }
}
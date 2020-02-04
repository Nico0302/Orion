package de.tum.www1.orion.util

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ToolWindowManager
import de.tum.www1.orion.ui.browser.Browser
import de.tum.www1.orion.util.OrionSettingsProvider.KEYS.ARTEMIS_URL
import de.tum.www1.orion.util.OrionSettingsProvider.KEYS.USER_AGENT

class OrionSettingsProviderService : OrionSettingsProvider {
    private val properties: PropertiesComponent
            get() = PropertiesComponent.getInstance()

    override fun saveSetting(key: OrionSettingsProvider.KEYS, setting: String) {
        // Reload page if URL changed
        if (key == ARTEMIS_URL && getSetting(ARTEMIS_URL) != setting || (key == USER_AGENT && getSetting(USER_AGENT) != setting)) {
            properties.setValue(key.toString(), setting)
            appService(ProjectManager::class.java).openProjects.forEach { project ->
                ToolWindowManager.getInstance(project).getToolWindow("Artemis").apply {
                    if (!isVisible) {
                        show(null)
                    }
                }
                project.service(Browser::class.java).init()
            }
            return
        }

        properties.setValue(key.toString(), setting)
    }

    override fun saveSettings(settings: MutableMap<OrionSettingsProvider.KEYS, String>) {
        settings.forEach { saveSetting(it.key, it.value) }
    }

    override fun getSetting(key: OrionSettingsProvider.KEYS): String = properties.getValue(key.toString(), key.defaultValue)

    override fun isModified(settings: Map<OrionSettingsProvider.KEYS, String>): Boolean {
        return settings.any { properties.getValue(it.key.toString()) != it.value }
    }
}

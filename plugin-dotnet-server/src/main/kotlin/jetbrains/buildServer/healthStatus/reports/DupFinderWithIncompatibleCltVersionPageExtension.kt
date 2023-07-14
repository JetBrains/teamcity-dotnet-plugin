package jetbrains.buildServer.healthStatus.reports

import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension

class DupFinderWithIncompatibleCltVersionPageExtension(
    pagePlaces: PagePlaces,
    pluginDescriptor: PluginDescriptor
) : HealthStatusItemPageExtension(DupFinderWithIncompatibleCltVersionReport.Type, pagePlaces) {
    init {
        includeUrl = pluginDescriptor.getPluginResourcesPath("dupFinderWithIncompatibleCltVersionReport.jsp")
        register()
    }
}

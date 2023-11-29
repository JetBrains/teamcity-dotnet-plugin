package jetbrains.buildServer.dotCover

import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.web.openapi.PluginDescriptor

class DotCoverRunType(
    private val _pluginDescriptor: PluginDescriptor,
    runTypeRegistry: RunTypeRegistry,
) : RunType() {
    init {
        _pluginDescriptor.pluginResourcesPath
        runTypeRegistry.registerRunType(this)
    }

    override fun getType() = "dotcover"

    override fun getDisplayName() = "dotCover"

    override fun getDescription() = ".NET code coverage tool"

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
        TODO("Not yet implemented")
    }

    override fun getEditRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("editDotCoverRunnerParameters.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("viewDotCoverRunnerParameters.jsp")

    override fun getDefaultRunnerProperties() = emptyMap<String, String>()

    override fun getTags() =
        mutableSetOf("dotCover", "coverage", "tests", "code", "unit", ".NET")

    override fun getIconUrl() =
        _pluginDescriptor.getPluginResourcesPath("dotcover.svg");
}
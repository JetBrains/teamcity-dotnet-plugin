package jetbrains.buildServer.script

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.util.*

class CSharpScriptRunType(
        runTypeRegistry: RunTypeRegistry,
        private val _pluginDescriptor: PluginDescriptor,
        private val _propertiesProcessor: PropertiesProcessor)
    : RunType() {

    init {
        runTypeRegistry.registerRunType(this)
    }

    override fun getRunnerPropertiesProcessor() =
        _propertiesProcessor

    override fun getDescription() = ScriptConstants.RUNNER_DESCRIPTION

    override fun getEditRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("editCSharpScriptRunParams.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("viewCSharpScriptRunParams.jsp")

    override fun getDefaultRunnerProperties() = emptyMap<String, String>()
        //mapOf(ScriptConstants.FRAMEWORK to Framework.Any.tfm)

    override fun getType() = ScriptConstants.RUNNER_TYPE

    override fun getDisplayName() = ScriptConstants.RUNNER_DISPLAY_NAME

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>) =
        runParameters[ScriptConstants.FRAMEWORK]
                ?.let { Framework.tryParse(it) }
                ?.requirement?.let { mutableListOf(it) }
                ?: mutableListOf()
}

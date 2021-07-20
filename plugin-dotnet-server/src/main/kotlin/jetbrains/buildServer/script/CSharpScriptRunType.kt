package jetbrains.buildServer.script

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.script.ScriptConstants.RUNNER_ENABLED
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.util.*

class CSharpScriptRunType(
        runTypeRegistry: RunTypeRegistry,
        private val _pluginDescriptor: PluginDescriptor,
        private val _propertiesProcessor: PropertiesProcessor,)
    : RunType() {

    init {
        if (TeamCityProperties.getBooleanOrTrue(RUNNER_ENABLED))
        {
            runTypeRegistry.registerRunType(this)
        }
    }

    override fun getRunnerPropertiesProcessor() =
        _propertiesProcessor

    override fun getDescription() = ScriptConstants.RUNNER_DESCRIPTION

    override fun getEditRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("editCSharpScriptRunParams.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("viewCSharpScriptRunParams.jsp")

    override fun describeParameters(parameters: Map<String?, String?>) =
        when(parameters[ScriptConstants.SCRIPT_TYPE]?.let { ScriptType.tryParse(it) } ?: ScriptType.Custom) {
            ScriptType.Custom -> "Custom script: " + customScriptDescription(parameters[ScriptConstants.SCRIPT_CONTENT])
            ScriptType.File -> "Script file: ${parameters[ScriptConstants.SCRIPT_FILE] ?: ""} ${parameters[ScriptConstants.ARGS] ?: ""}"
        }

    override fun getDefaultRunnerProperties() = emptyMap<String, String>()
        //mapOf(ScriptConstants.FRAMEWORK to Framework.Any.tfm)

    override fun getType() = ScriptConstants.RUNNER_TYPE

    override fun getDisplayName() = ScriptConstants.RUNNER_DISPLAY_NAME

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>) =
            (
                    runParameters[ScriptConstants.FRAMEWORK]
                            ?.let { Framework.tryParse(it) }
                            ?: Framework.Any
                    )
                    .requirement.let { mutableListOf(it) }


    private fun customScriptDescription(scriptContent: String?):String {
        if (scriptContent.isNullOrBlank()) {
            return "<empty>"
        }
        else {
            val scriptLines = scriptContent.lines().dropWhile { it.isNullOrBlank() }
            return when(scriptLines.size) {
                0 -> "<empty>"
                1 -> scriptLines[0]
                else -> scriptLines[0] + " (and ${scriptLines.size - 1} more lines)"
            }
        }
    }
}

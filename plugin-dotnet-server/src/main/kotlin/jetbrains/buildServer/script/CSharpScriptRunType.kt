package jetbrains.buildServer.script

import jetbrains.buildServer.*
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.script.ScriptConstants.CLT_PATH
import jetbrains.buildServer.script.ScriptConstants.CLT_TOOL_TYPE_ID
import jetbrains.buildServer.script.ScriptConstants.RUNNER_ENABLED
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.web.openapi.PluginDescriptor

class CSharpScriptRunType(
        runTypeRegistry: RunTypeRegistry,
        private val _pluginDescriptor: PluginDescriptor,
        private val _propertiesProcessor: PropertiesProcessor,
        private val _toolVersionProvider: ToolVersionProvider)
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

    override fun getType() = ScriptConstants.RUNNER_TYPE

    override fun getDisplayName() = ScriptConstants.RUNNER_DISPLAY_NAME

    override fun getRunnerSpecificRequirements(parameters: Map<String, String>): MutableList<Requirement> {
        val toolVersion = _toolVersionProvider.getVersion(parameters[CLT_PATH], CLT_TOOL_TYPE_ID)
        return Ranges.filter { it.range.contains(toolVersion) }.firstOrNull()?.requirement?.let { mutableListOf(it) } ?: mutableListOf(createRequitement("6\\."))
    }

    fun getTags(): MutableSet<String> {
        return mutableSetOf(".NET", "C#", "script")
    }

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

    companion object {
        private fun createRequitement(vararg versions: String) =
                Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME}(${versions.joinToString("|")})[\\d\\.]+${DotnetConstants.CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)

        private val Ranges = listOf(
                VersionRange(Version(0).including() to Version(1, 0, 3).including(), createRequitement("6\\.")),
                VersionRange(Version(1, 0, 3).excluding() to Version(Int.MAX_VALUE).including(), createRequitement("6\\.", "7\\."))
        )
    }

    private data class VersionRange(val range: Range<Version>, val requirement: Requirement)
}

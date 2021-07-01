package jetbrains.buildServer.script

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TargetDotNetFramework
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
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

    override fun getDefaultRunnerProperties(): Map<String, String> = HashMap()

    override fun getType() = ScriptConstants.RUNNER_TYPE

    override fun getDisplayName() = ScriptConstants.RUNNER_DISPLAY_NAME

    override fun describeParameters(parameters: Map<String, String>): String {
        val sb = StringBuilder()

/*
        val solutionPath = parameters[InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH]
        val projectFilter = parameters[InspectCodeConstants.RUNNER_SETTING_PROJECT_FILTER]
        if (!StringUtil.isEmptyOrSpaces(solutionPath)) {
            sb.append("Solution file path: ").append(solutionPath).append("\n")
        }

        sb.append("Sources to analyze: ")
        if (StringUtil.isEmptyOrSpaces(projectFilter)) {
            sb.append("whole solution").append("\n")
        } else {
            sb.append(projectFilter).append("\n")
        }
*/

        return sb.toString()
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>) = mutableListOf(DotnetCore3)

    companion object {
        private val DotnetCore3 = Requirement(RequirementQualifier.EXISTS_QUALIFIER + "${DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME}3\\.[\\d\\.]+${DotnetConstants.CONFIG_SUFFIX_PATH}", null, RequirementType.EXISTS)
    }
}

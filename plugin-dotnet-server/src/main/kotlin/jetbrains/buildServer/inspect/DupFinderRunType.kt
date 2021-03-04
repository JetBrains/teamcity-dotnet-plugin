package jetbrains.buildServer.inspect

import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.util.*

class DupFinderRunType(
        runTypeRegistry: RunTypeRegistry,
        private val myPluginDescriptor: PluginDescriptor,
        private val myRequirementsProvider: RequirementsProvider) : RunType() {
    override fun getRunnerPropertiesProcessor(): PropertiesProcessor {
        return DupFinderRunTypePropertiesProcessor()
    }

    override fun getDescription(): String {
        return DupFinderConstants.RUNNER_DESCRIPTION
    }

    override fun getEditRunnerParamsJspFilePath(): String {
        return myPluginDescriptor.getPluginResourcesPath("editDupFinderRunParams.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String {
        return myPluginDescriptor.getPluginResourcesPath("viewDupFinderRunParams.jsp")
    }

    override fun getDefaultRunnerProperties(): Map<String, String> {
        val parameters: MutableMap<String, String> = HashMap()
        parameters[DupFinderConstants.SETTINGS_INCLUDE_FILES] = "**/*.cs"
        parameters[DupFinderConstants.SETTINGS_DISCARD_COST] = "70"
        parameters[DupFinderConstants.SETTINGS_DISCARD_LITERALS] = true.toString()
        return parameters
    }

    override fun getType(): String {
        return DupFinderConstants.RUNNER_TYPE
    }

    override fun getDisplayName(): String {
        return DupFinderConstants.RUNNER_DISPLAY_NAME
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        val includes = parameters[DupFinderConstants.SETTINGS_INCLUDE_FILES]
        val excludes = parameters[DupFinderConstants.SETTINGS_EXCLUDE_FILES]
        val sb = StringBuilder()
        if (!StringUtil.isEmptyOrSpaces(includes)) {
            sb.append("Include sources: ").append(includes).append("\n")
        }
        if (!StringUtil.isEmptyOrSpaces(excludes)) {
            sb.append("Exclude sources: : ").append(excludes).append("\n")
        }
        return sb.toString()
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>): List<Requirement> {
        return myRequirementsProvider.getRequirements(runParameters).toList()
    }

    init {
        runTypeRegistry.registerRunType(this)
    }
}

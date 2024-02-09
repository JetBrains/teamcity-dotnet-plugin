

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor

/**
 * Dotnet runner definition.
 */
class DotnetRunnerRunType(
    runTypeRegistry: RunTypeRegistry,
    private val _pluginDescriptor: PluginDescriptor,
    private val _dotnetRunnerRequirementsProvider: RequirementsProvider,
) : RunType() {

    init {
        _pluginDescriptor.pluginResourcesPath
        runTypeRegistry.registerRunType(this)
    }

    override fun getType(): String {
        return DotnetConstants.RUNNER_TYPE
    }

    override fun getDisplayName(): String {
        return DotnetConstants.RUNNER_DISPLAY_NAME
    }

    override fun getDescription(): String {
        return DotnetConstants.RUNNER_DESCRIPTION
    }

    override fun getRunnerPropertiesProcessor(): PropertiesProcessor {
        return PropertiesProcessor { properties ->
            val command = properties?.get(DotnetConstants.PARAM_COMMAND)
            if (command.isNullOrEmpty()) {
                return@PropertiesProcessor arrayListOf(InvalidProperty(DotnetConstants.PARAM_COMMAND, "Command must be set"))
            }

            val errors = arrayListOf<InvalidProperty>()
            DotnetParametersProvider.commandTypes[command]?.let {
                errors.addAll(it.validateProperties(properties))
            }

            properties[CoverageConstants.PARAM_TYPE]?.let {
                DotnetParametersProvider.coverageTypes[it]?.let {
                    errors.addAll(it.validateProperties(properties))
                }
            }

            errors
        }
    }

    override fun getEditRunnerParamsJspFilePath(): String {
        return _pluginDescriptor.getPluginResourcesPath("editDotnetParameters.jsp")
    }

    override fun getViewRunnerParamsJspFilePath(): String {
        return _pluginDescriptor.getPluginResourcesPath("viewDotnetParameters.jsp")
    }

    override fun getDefaultRunnerProperties() = mapOf(
        CoverageConstants.PARAM_DOTCOVER_MERGE to "true",
        CoverageConstants.PARAM_DOTCOVER_REPORT to "true"
    )

    override fun describeParameters(parameters: Map<String, String>): String {
        val paths = (parameters[DotnetConstants.PARAM_PATHS] ?: "").trim()
        val commandName = parameters[DotnetConstants.PARAM_COMMAND]?.replace('-', ' ')
        val args = parameters[DotnetConstants.PARAM_ARGUMENTS]?.let {
            StringUtil.splitCommandArgumentsAndUnquote(it).take(2).joinToString(" ")
        } ?: ""

        return when {
            commandName == DotnetCommandType.Custom.id -> "$paths\nCommand line parameters: $args"
            !commandName.isNullOrBlank() -> "$commandName $paths"
            else -> args
        }
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>) =
        _dotnetRunnerRequirementsProvider.getRequirements(runParameters).toList()

    override fun getTags(): MutableSet<String> {
        return mutableSetOf(".NET", "MSBuild", "VSTest", "VS", "Visual Studio", "NuGet", "devenv")
    }

    override fun getIconUrl(): String {
        return _pluginDescriptor.getPluginResourcesPath("dotnet-runner.svg");
    }
}
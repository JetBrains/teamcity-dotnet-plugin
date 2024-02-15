package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetParametersProvider
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.web.functions.InternalProperties
import jetbrains.buildServer.web.openapi.PluginDescriptor
import javax.swing.plaf.basic.BasicInternalFrameUI.InternalFramePropertyChangeListener

class DotCoverRunnerRunType(
    runTypeRegistry: RunTypeRegistry,
    private val _pluginDescriptor: PluginDescriptor,
    private val _dotCoverRequirementsProvider: DotCoverRequirementsProvider,
) : RunType() {
    init {
        if (InternalProperties.getBoolean(DotnetConstants.PARAM_DOTCOVER_RUNNER_ENABLED) ?: false) {
            runTypeRegistry.registerRunType(this)
        }
    }

    override fun getType() = CoverageConstants.DOTCOVER_RUNNER_TYPE

    override fun getDisplayName() = "dotCover"

    override fun getDescription() = ".NET code coverage tool"

    override fun getEditRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("editDotCoverRunnerParameters.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("viewDotCoverRunnerParameters.jsp")

    override fun getTags() =
        mutableSetOf("dotCover", "coverage", "tests", "code", "unit", ".NET", "profiler", "JetBrains")

    override fun getIconUrl() =
        _pluginDescriptor.getPluginResourcesPath("dotcover.svg");

    override fun getDefaultRunnerProperties() = mapOf(
        CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT to "true",
    )

    override fun supports(runTypeExtension: RunTypeExtension) = when {
        runTypeExtension.isContainerWrapper()-> true
        else -> super.supports(runTypeExtension)
    }

    // properties validation
    override fun getRunnerPropertiesProcessor(): PropertiesProcessor {
        return PropertiesProcessor { properties ->
            val dotCoverHome = properties?.get(CoverageConstants.PARAM_DOTCOVER_HOME)
            if (dotCoverHome.isNullOrEmpty()) {
                return@PropertiesProcessor arrayListOf(
                    InvalidProperty(DotnetConstants.PARAM_COMMAND, "dotCover home path must be set")
                )
            }

            val hasCoveringCommandLine = properties.get(CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE).isNullOrBlank().not()
            val shouldGenerateReport = properties.get(CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT).toBoolean()
            val hasAdditionalSnapshotPaths = properties.get(CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS)?.trim().isNullOrBlank().not()

            val noOptionsSelected = !shouldGenerateReport && !hasCoveringCommandLine && !hasAdditionalSnapshotPaths

            when {
                noOptionsSelected -> arrayListOf(
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE, NO_OPTION_SELECTED_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT, NO_OPTION_SELECTED_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS, NO_OPTION_SELECTED_ERROR),
                )

                else -> emptyList<InvalidProperty>()
            }
        }
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        val commandLine = parameters[CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE]
            ?.trim()?.let { StringUtil.splitCommandArgumentsAndUnquote(it).take(5).joinToString(" ") } ?: ""
        val shouldGenerateReport = parameters[CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT].toBoolean()
        val hasAdditionalSnapshotPaths = parameters[CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS].toBoolean()
        val containerImage = parameters[DotnetConstants.PARAM_DOCKER_IMAGE]?.trim() ?: ""

        return buildString {
            if (commandLine.isNotBlank()) appendLine("Cover command line: $commandLine")
            if (shouldGenerateReport) appendLine("Generate report")
            if (hasAdditionalSnapshotPaths) appendLine("Include additional dotCover snapshots to the report")
            if (containerImage.isNotBlank()) appendLine("Container image: $containerImage")
        }
    }
    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>): List<Requirement> =
        _dotCoverRequirementsProvider.getRequirements(runParameters).toList()

    companion object {
        const val NO_OPTION_SELECTED_ERROR =
            "At least one of the fields \"Command Line\", \"Generate report\", " +
            "\"Join reports from previous build steps\", " +
            "or \"Include additional dotCover snapshots to the report\" must be set"

        fun RunTypeExtension.isContainerWrapper() =
            this is PositionAware && this.orderId == "dockerWrapper"
    }
}
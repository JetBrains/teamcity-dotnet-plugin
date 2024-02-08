package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetParametersProvider
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor

class DotCoverRunnerRunType(
    runTypeRegistry: RunTypeRegistry,
    private val _pluginDescriptor: PluginDescriptor,
    private val _dotCoverRequirementsProvider: DotCoverRequirementsProvider,
) : RunType() {
    init {
        _pluginDescriptor.pluginResourcesPath
        runTypeRegistry.registerRunType(this)
    }

    override fun getType() = "dotcover"

    override fun getDisplayName() = "dotCover"

    override fun getDescription() = ".NET code coverage tool"

    override fun getEditRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("editDotCoverRunnerParameters.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("viewDotCoverRunnerParameters.jsp")

    override fun getDefaultRunnerProperties() = emptyMap<String, String>()

    override fun getTags() =
        mutableSetOf("dotCover", "coverage", "tests", "code", "unit", ".NET")

    override fun getIconUrl() =
        _pluginDescriptor.getPluginResourcesPath("dotcover.svg");

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
            val shouldMergeSnapshots = properties.get(CoverageConstants.PARAM_DOTCOVER_MERGE_SNAPSHOTS).toBoolean()
            val hasAdditionalSnapshotPaths = properties.get(CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS)?.trim().isNullOrBlank().not()

            val hasSnapshots = shouldMergeSnapshots || hasAdditionalSnapshotPaths
            val nothingToReport = shouldGenerateReport && !hasCoveringCommandLine && !hasSnapshots
            val noOptionsSelected = !shouldGenerateReport && !hasCoveringCommandLine && !hasSnapshots

            when {
                nothingToReport -> arrayListOf(
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE, NOTHING_TO_REPORT_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT, NOTHING_TO_REPORT_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_MERGE_SNAPSHOTS, NOTHING_TO_REPORT_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS, NOTHING_TO_REPORT_ERROR),
                )

                noOptionsSelected -> arrayListOf(
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE, NO_OPTION_SELECTED_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT, NO_OPTION_SELECTED_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_MERGE_SNAPSHOTS, NO_OPTION_SELECTED_ERROR),
                    InvalidProperty(CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS, NO_OPTION_SELECTED_ERROR),
                )

                else -> emptyList<InvalidProperty>()
            }
        }
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        val commandLine = parameters[CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE]
            ?.trim()?.let {
                StringUtil.splitCommandArgumentsAndUnquote(it).take(5).joinToString(" ")
            } ?: ""
        val shouldGenerateReport = parameters[CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT].toBoolean()
        val shouldMergeSnapshots = parameters[CoverageConstants.PARAM_DOTCOVER_MERGE_SNAPSHOTS].toBoolean()
        val hasAdditionalSnapshotPaths = parameters[CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS].toBoolean()

        return buildString {
            if (commandLine.isNotBlank()) appendLine("Cover command line: $commandLine")
            if (shouldGenerateReport) appendLine("Generate report")
            if (shouldMergeSnapshots) appendLine("Join reports from previous build steps")
            if (hasAdditionalSnapshotPaths) appendLine("Include additional dotCover snapshots to the report")
        }
    }
    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>): List<Requirement> =
        _dotCoverRequirementsProvider.getRequirements(runParameters).toList()

    companion object {
        const val NOTHING_TO_REPORT_ERROR =
            "The \"Generate report\" option is set, but neither a covered command line " +
            "nor a single report is included in the report"

        const val NO_OPTION_SELECTED_ERROR =
            "At least one of the fields \"Command Line\", \"Generate report\", " +
            "\"Join reports from previous build steps\", " +
            "or \"Include additional dotCover snapshots to the report\" must be set"
    }
}
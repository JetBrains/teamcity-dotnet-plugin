package jetbrains.buildServer.nunit

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotCover.DotCoverRunnerProperties
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.web.functions.InternalProperties
import jetbrains.buildServer.web.openapi.PluginDescriptor

class NUnitRunType(
    private val _descriptor: PluginDescriptor,
    private val _requirementsProvider: RequirementsProvider,
    runTypeRegistry: RunTypeRegistry
) : RunType() {
    init {
        if (InternalProperties.getBooleanOrTrue(NUnitRunnerConstants.NUNIT_RUNNER_ENABLED)) {
            runTypeRegistry.registerRunType(this)
        }
    }

    override fun getType(): String = NUnitRunnerConstants.NUNIT_RUN_TYPE

    override fun getDisplayName(): String = "NUnit"

    override fun getDescription(): String = "Runs NUnit tests"

    override fun describeParameters(parameters: Map<String, String>) = buildString {
        val sb = StringBuilder()
        buildParameter(sb, "Run tests on", parameters[NUnitRunnerConstants.NUNIT_TESTS_FILES_INCLUDE])
        buildParameter(sb, "Excluding", parameters[NUnitRunnerConstants.NUNIT_TESTS_FILES_EXCLUDE])
        return sb.toString()
    }

    private fun buildParameter(sb: StringBuilder, descr: String, value: String?) {
        if (value.isNullOrBlank()) return
        sb.append(descr).append(": ").append(value).append("\n")
    }

    override fun getRunnerPropertiesProcessor() = PropertiesProcessor { properties ->

        if (properties[NUnitRunnerConstants.NUNIT_PATH].isNullOrBlank()) {
            return@PropertiesProcessor listOf<InvalidProperty>(
                InvalidProperty(
                    NUnitRunnerConstants.NUNIT_PATH,
                    "Path to NUnit console tool should be defined"
                )
            )
        }

        if (properties[NUnitRunnerConstants.NUNIT_TESTS_FILES_INCLUDE].isNullOrBlank()) {
            return@PropertiesProcessor listOf<InvalidProperty>(
                InvalidProperty(
                    NUnitRunnerConstants.NUNIT_TESTS_FILES_INCLUDE,
                    "Files to test should be defined"
                )
            )
        }
        emptyList()
    }

    override fun getRunnerSpecificRequirements(runParameters: MutableMap<String, String>) =
        _requirementsProvider.getRequirements(runParameters).toList()

    override fun getEditRunnerParamsJspFilePath() =
        _descriptor.getPluginResourcesPath("nunit/editNUnitRunnerParameters.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _descriptor.getPluginResourcesPath("nunit/viewNUnitParameters.jsp")

    override fun getDefaultRunnerProperties(): Map<String, String> = DotCoverRunnerProperties.getDefaultRunnerProperties()

    override fun getTags() = setOf(".NET", "dotnet")

    override fun getIconUrl() = _descriptor.getPluginResourcesPath("nunit/nunit-runner.svg")

    override fun supports(runTypeExtension: RunTypeExtension) = when {
        runTypeExtension is PositionAware && runTypeExtension.orderId == "dockerWrapper" -> true
        else -> super.supports(runTypeExtension)
    }
}

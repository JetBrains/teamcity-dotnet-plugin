package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet MSBuild command.
 */
class VSTestCommandType(
        private val _vstestRequirementsProvider: VSTestRequirementsProvider,
        private val _dotCoverInfoProvider: DotCoverInfoProvider) : CommandType() {
    override val name: String
        get() = DotnetCommandType.VSTest.id

    override val editPage: String
        get() = "editVSTestParameters.jsp"

    override val viewPage: String
        get() = "viewVSTestParameters.jsp"

    override fun validateProperties(properties: Map<String, String>): Collection<InvalidProperty> {
        val invalidProperties = arrayListOf<InvalidProperty>()
        if (_dotCoverInfoProvider.isCoverageEnabled(properties)) {
            if (properties[DotCoverConstants.PARAM_HOME].isNullOrBlank()) {
                invalidProperties.add(InvalidProperty(DotCoverConstants.PARAM_HOME, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        return invalidProperties
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun getRequirements(runParameters: Map<String, String>) = _vstestRequirementsProvider.getRequirements(runParameters)
}
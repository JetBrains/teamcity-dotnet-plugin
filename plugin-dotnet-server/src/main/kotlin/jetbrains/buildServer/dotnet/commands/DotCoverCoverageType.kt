

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotCover.DotCoverToolVersionType
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_PACKAGE_ID
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_PATTERN_3_5
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_4_6_1_PATTERN
import jetbrains.buildServer.dotnet.CoverageConstants.DOTNET_FRAMEWORK_4_7_2_PATTERN
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER_HOME
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_TYPE
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.RequirementFactory
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.tools.ToolVersion
import org.springframework.beans.factory.BeanFactory

class DotCoverCoverageType(requirementFactory: RequirementFactory): CommandType(requirementFactory) {
    override val name: String = PARAM_DOTCOVER

    override val description: String = "JetBrains dotCover"

    override val editPage: String = "editDotCoverParameters.jsp"

    override val viewPage: String = "viewDotCoverParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[PARAM_DOTCOVER_HOME].isNullOrBlank()) {
            yield(InvalidProperty(PARAM_DOTCOVER_HOME, DotnetConstants.VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        yieldAll(super.getRequirements(parameters, factory))

        if (parameters[PARAM_TYPE] != PARAM_DOTCOVER) {
            return@sequence
        }

        val toolVersion = getToolVersion(parameters, factory) ?: return@sequence

        // set agent requirement in accordance to dotCover version
        when (DotCoverToolVersionType.determine(toolVersion.version)) {
            DotCoverToolVersionType.UsingBundledRuntime -> {
                return@sequence     // no requirements since all necessary software bundled with the tool
            }

            DotCoverToolVersionType.UsingAgentRuntime ->
                // no requirements since currently there is no a good way to make composite agent requirements
                // that could express something like: `(Windows AND .NET Framework) OR ((Linux OR macOS) AND .NET SDK)`;
                // in case of incompatibility of agents a warning will be produced on the build time
                return@sequence

            DotCoverToolVersionType.UsingDotNetFramework472 ->
                yield(DOTNET_FRAMEWORK_472_REQUIREMENT)

            DotCoverToolVersionType.UsingDotNetFramework461 ->
                yield(DOTNET_FRAMEWORK_461_REQUIREMENT)

            DotCoverToolVersionType.UsingDotNetFramework40 ->
                yield(DOTNET_FRAMEWORK_35_OR_40_REQUIREMENT)    // for compatibility with old builds

            else -> return@sequence
        }
    }

    private fun getToolVersion(parameters: Map<String, String>, factory: BeanFactory): ToolVersion? {
        val dotCoverHomeValue = parameters[PARAM_DOTCOVER_HOME] ?: return null
        val toolManager = factory.getBean(ServerToolManager::class.java)
        val toolType = toolManager.findToolType(DOTCOVER_PACKAGE_ID) ?: return null
        val projectManager = factory.getBean(ProjectManager::class.java)
        return toolManager.resolveToolVersionReference(toolType, dotCoverHomeValue, projectManager.rootProject)
    }

    companion object {
        private val DOTNET_FRAMEWORK_35_OR_40_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_PATTERN_3_5)", null, RequirementType.EXISTS)

        private val DOTNET_FRAMEWORK_461_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_4_6_1_PATTERN)", null, RequirementType.EXISTS)

        private val DOTNET_FRAMEWORK_472_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_4_7_2_PATTERN)", null, RequirementType.EXISTS)

    }
}
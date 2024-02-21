package jetbrains.buildServer.dotCover

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotNet.DotNetConstants
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager
import jetbrains.buildServer.tools.ToolVersion
import jetbrains.buildServer.dotnet.DotnetConstants

class DotCoverRequirementsProvider(
    private val _projectManager: ProjectManager,
    private val _toolManager: ServerToolManager
) : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>) = sequence {
        // no requirements in a container
        if (!parameters[DotnetConstants.PARAM_DOCKER_IMAGE].isNullOrEmpty()) {
            return@sequence
        }

        val toolVersion = getToolVersion(parameters) ?: return@sequence

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

    private fun getToolVersion(parameters: Map<String, String>): ToolVersion? {
        val dotCoverHomeValue = parameters[CoverageConstants.PARAM_DOTCOVER_HOME] ?: return null
        val toolType = _toolManager.findToolType(CoverageConstants.DOTCOVER_PACKAGE_ID) ?: return null
        return _toolManager.resolveToolVersionReference(toolType, dotCoverHomeValue, _projectManager.rootProject)
    }

    companion object {
        internal const val DOTNET_FRAMEWORK_472_PATTERN = DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.(7\\.([2-9]|[\\d]{2,})|[8-9]|[\\d]{2,})[\\d\\.]*_.+"
        internal const val DOTNET_FRAMEWORK_461_PATTERN = DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_.+"
        internal val DOTNET_FRAMEWORK_35_OR_40_PATTERN = DotNetConstants.DOTNET_FRAMEWORK_3_5.replace(".", "\\.") + "_.+|" + DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.[\\d\\.]+_.+"

        private val DOTNET_FRAMEWORK_472_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_472_PATTERN)", null, RequirementType.EXISTS)

        private val DOTNET_FRAMEWORK_461_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_461_PATTERN)", null, RequirementType.EXISTS)

        private val DOTNET_FRAMEWORK_35_OR_40_REQUIREMENT =
            Requirement("${RequirementQualifier.EXISTS_QUALIFIER}($DOTNET_FRAMEWORK_35_OR_40_PATTERN)", null, RequirementType.EXISTS)
    }
}
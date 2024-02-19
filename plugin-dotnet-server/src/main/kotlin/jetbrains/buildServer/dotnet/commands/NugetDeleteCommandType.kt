package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.requirements.SDKBasedRequirementFactory
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides parameters for dotnet nuget delete command.
 */
class NugetDeleteCommandType(
    sdkBasedRequirementFactory: SDKBasedRequirementFactory
) : DotnetCLICommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.NuGetDelete.id

    override val description: String = name.replace('-', ' ')

    override val editPage: String = "editNugetDeleteParameters.jsp"

    override val viewPage: String = "viewNugetDeleteParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        DotnetConstants.PARAM_NUGET_PACKAGE_ID.let {
            if (properties[it].isNullOrBlank()) {
                yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        DotnetConstants.PARAM_NUGET_PACKAGE_SOURCE.let {
            if (properties[it].isNullOrBlank()) {
                yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        DotnetConstants.PARAM_NUGET_API_KEY.let {
            if (properties[it].isNullOrBlank()) {
                yield(InvalidProperty(it, DotnetConstants.VALIDATION_EMPTY))
            }
        }
    }
}
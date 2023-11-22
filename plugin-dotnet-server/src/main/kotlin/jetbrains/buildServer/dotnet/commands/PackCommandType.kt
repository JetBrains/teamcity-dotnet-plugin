

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.RequirementFactory
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.springframework.beans.factory.BeanFactory

/**
 * Provides parameters for dotnet pack command.
 */
class PackCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetCLICommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Pack.id

    override val editPage: String = "editPackParameters.jsp"

    override val viewPage: String = "viewPackParameters.jsp"

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        yieldAll(super.getRequirements(parameters, factory))

        if (!parameters[DotnetConstants.PARAM_RUNTIME].isNullOrBlank()) {
            yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI, "2.0.0", RequirementType.VER_NO_LESS_THAN))
        }
    }
}
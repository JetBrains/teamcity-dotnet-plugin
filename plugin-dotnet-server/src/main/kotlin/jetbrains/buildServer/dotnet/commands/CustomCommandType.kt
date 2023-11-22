

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import org.springframework.beans.factory.BeanFactory

/**
 * Provides parameters for dotnet %custom% command.
 */
class CustomCommandType(
        private val _requirementFactory: RequirementFactory)
    : DotnetCommandType(_requirementFactory) {
    override val name: String = DotnetCommandType.Custom.id

    override val description: String = "<custom>"

    override val editPage: String = "editCustomParameters.jsp"

    override val viewPage: String = "viewCustomParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[DotnetConstants.PARAM_PATHS].isNullOrBlank() && properties[DotnetConstants.PARAM_ARGUMENTS].isNullOrBlank()) {
            yield(InvalidProperty(DotnetConstants.PARAM_PATHS, VALIDATION_EMPTY))
            yield(InvalidProperty(DotnetConstants.PARAM_ARGUMENTS, VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory): Sequence<Requirement> = sequence {
        yieldAll(super.getRequirements(parameters, factory))
        
        if (parameters[DotnetConstants.PARAM_PATHS].isNullOrBlank()) {
            yieldAll(super.getRequirements(parameters, factory))
        }
    }

    companion object {
        const val VALIDATION_EMPTY: String = "Either the \"Executables\" field or \"Command line parameters\" should not be empty"
    }
}
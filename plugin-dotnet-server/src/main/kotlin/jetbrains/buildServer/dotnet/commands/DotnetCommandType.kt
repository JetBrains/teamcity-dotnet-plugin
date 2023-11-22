package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.CommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.RequirementFactory
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.springframework.beans.factory.BeanFactory

abstract class DotnetCommandType(requirementFactory: RequirementFactory) : CommandType(requirementFactory) {
    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        yieldAll(super.getRequirements(parameters, factory))
        yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS))
    }
}
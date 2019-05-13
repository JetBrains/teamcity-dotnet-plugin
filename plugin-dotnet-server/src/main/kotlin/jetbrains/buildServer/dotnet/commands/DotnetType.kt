package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import org.springframework.beans.factory.BeanFactory

abstract class DotnetType : CommandType() {
    override fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) = sequence {
        if (isDocker(parameters)) return@sequence

        yield(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
    }
}
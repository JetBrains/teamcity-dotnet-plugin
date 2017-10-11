package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

abstract class DotnetType : CommandType() {
    override fun getRequirements(runParameters: Map<String, String>) = buildSequence {
        yield(Requirement(DotnetConstants.CONFIG_PATH, null, RequirementType.EXISTS))
    }
}
package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.requirements.SDKBasedRequirementFactory

abstract class DotnetCLICommandType(
    sdkBasedRequirementFactory: SDKBasedRequirementFactory,
) : CommandType(sdkBasedRequirementFactory)
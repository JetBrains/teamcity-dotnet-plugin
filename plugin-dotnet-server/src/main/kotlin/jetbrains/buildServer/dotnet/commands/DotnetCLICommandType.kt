package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory

abstract class DotnetCLICommandType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory,
) : CommandType(sdkBasedRequirementFactory)
package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.arguments.CommandTarget
import jetbrains.buildServer.dotnet.arguments.TargetService

class TargetServiceStub(override val targets: Sequence<CommandTarget>) : TargetService {
}
package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.TargetService

class TargetServiceStub(override val targets: Sequence<CommandTarget>) : TargetService {
}


package jetbrains.buildServer.dotnet.test.dotnet.commands.targeting

import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.commands.targeting.TargetService

class TargetServiceStub(override val targets: Sequence<CommandTarget>) : TargetService
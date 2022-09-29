package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.dotnet.CommandTargetType
import java.io.File

interface TargetTypeProvider {
    fun getTargetType(file: File): CommandTargetType
}
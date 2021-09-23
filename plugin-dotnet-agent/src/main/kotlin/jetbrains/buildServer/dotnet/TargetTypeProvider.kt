package jetbrains.buildServer.dotnet

import java.io.File

interface TargetTypeProvider {
    fun getTargetType(file: File): CommandTargetType
}
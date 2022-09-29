package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.dotnet.CommandTargetType
import java.io.File

class TargetTypeProviderImpl : TargetTypeProvider {
    override fun getTargetType(file: File) =
        when {
            "dll".equals(file.extension, true) -> {
                CommandTargetType.Assembly
            }
            else -> {
                CommandTargetType.Unknown
            }
        }
}
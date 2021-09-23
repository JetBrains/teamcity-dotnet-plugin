package jetbrains.buildServer.dotnet

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
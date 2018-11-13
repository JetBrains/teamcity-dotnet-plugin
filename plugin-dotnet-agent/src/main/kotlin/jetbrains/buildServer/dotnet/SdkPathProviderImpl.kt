package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.util.OSType
import java.io.File

class SdkPathProviderImpl(
        private val _toolProvider: ToolProvider,
        private val _environment: Environment)
    : SdkPathProvider {
    override val path: File
        get() {
            val sdkRootPath = when(_environment.os) {
                OSType.WINDOWS -> File(_toolProvider.getPath(DotnetConstants.EXECUTABLE)).parentFile ?: File(".")
                OSType.UNIX -> File("/usr/share/dotnet")
                OSType.MAC -> File("/usr/local/share/dotnet")
            }

            return File(
                    sdkRootPath,
                    "sdk")
        }
}
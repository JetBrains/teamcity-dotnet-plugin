package jetbrains.buildServer.dotnet

import java.io.File

interface TestRunSettingsFileProvider {
    fun tryGet(command: DotnetCommandType): File?
}
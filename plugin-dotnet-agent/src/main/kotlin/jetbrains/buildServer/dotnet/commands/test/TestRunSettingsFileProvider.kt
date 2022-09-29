package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.DotnetCommandType
import java.io.File

interface TestRunSettingsFileProvider {
    fun tryGet(command: DotnetCommandType): File?
}
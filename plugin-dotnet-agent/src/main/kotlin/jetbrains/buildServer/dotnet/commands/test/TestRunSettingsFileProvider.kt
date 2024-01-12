

package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.DotnetCommandContext
import java.io.File

interface TestRunSettingsFileProvider {
    fun tryGet(context: DotnetCommandContext): File?
}
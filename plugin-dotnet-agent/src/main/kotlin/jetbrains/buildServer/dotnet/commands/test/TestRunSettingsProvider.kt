package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.DotnetCommandType
import org.w3c.dom.Document

interface TestRunSettingsProvider {
    fun tryCreate(command: DotnetCommandType): Document?
}
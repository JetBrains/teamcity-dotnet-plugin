package jetbrains.buildServer.dotnet

import org.w3c.dom.Document

interface TestRunSettingsProvider {
    fun tryCreate(command: DotnetCommandType): Document?
}
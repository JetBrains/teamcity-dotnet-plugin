

package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.DotnetCommandContext
import org.w3c.dom.Document

interface TestRunSettingsProvider {
    fun tryCreate(context: DotnetCommandContext): Document?
}
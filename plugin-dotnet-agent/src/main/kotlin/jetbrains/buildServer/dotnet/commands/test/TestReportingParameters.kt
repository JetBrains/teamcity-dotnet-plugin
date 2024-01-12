

package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.TestReportingMode
import java.util.*

interface TestReportingParameters {
    fun getMode(context: DotnetCommandContext): EnumSet<TestReportingMode>
}
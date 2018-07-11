package jetbrains.buildServer.dotnet

import java.util.*

interface TestReportingParameters {
    fun getMode(context: DotnetBuildContext): EnumSet<TestReportingMode>
}
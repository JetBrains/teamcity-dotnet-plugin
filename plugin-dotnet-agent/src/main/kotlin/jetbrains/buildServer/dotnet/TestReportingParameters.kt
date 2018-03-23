package jetbrains.buildServer.dotnet

import java.util.*

interface TestReportingParameters {
    val Mode: EnumSet<TestReportingMode>
}
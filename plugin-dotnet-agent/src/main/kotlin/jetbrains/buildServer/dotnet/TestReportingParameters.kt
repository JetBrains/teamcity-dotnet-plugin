package jetbrains.buildServer.dotnet

import java.util.*

interface TestReportingParameters {
    val mode: EnumSet<TestReportingMode>
}
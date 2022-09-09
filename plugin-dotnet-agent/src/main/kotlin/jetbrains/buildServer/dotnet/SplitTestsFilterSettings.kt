package jetbrains.buildServer.dotnet

import java.io.File

interface SplitTestsFilterSettings {
    val isActive: Boolean
    val filterType: SplittedTestsFilterType
    val testsClassesFile: File?
    val testClasses: List<String>
    val useExactMatchFilter: Boolean
    val exactMatchFilterSize: Int
}

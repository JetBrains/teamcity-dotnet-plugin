package jetbrains.buildServer.dotnet

import java.io.File

interface SplitTestsFilterSettings {
    val isActive: Boolean
    val filterType: SplittedTestsFilterType
    val testClasses: Sequence<String>
    val useExactMatchFilter: Boolean
    val exactMatchFilterSize: Int
}

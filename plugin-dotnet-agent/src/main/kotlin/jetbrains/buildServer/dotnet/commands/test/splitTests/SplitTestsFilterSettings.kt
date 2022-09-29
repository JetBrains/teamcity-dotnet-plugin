package jetbrains.buildServer.dotnet.commands.test.splitTests

interface SplitTestsFilterSettings {
    val isActive: Boolean
    val filterType: SplitTestsFilterType
    val testClasses: Sequence<String>
    val useExactMatchFilter: Boolean
    val exactMatchFilterSize: Int
}

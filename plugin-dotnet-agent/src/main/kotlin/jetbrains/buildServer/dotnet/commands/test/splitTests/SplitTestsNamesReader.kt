package jetbrains.buildServer.dotnet.commands.test.splitTests

interface SplitTestsNamesReader {
    fun read(): Sequence<String>
}
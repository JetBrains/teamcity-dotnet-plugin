package jetbrains.buildServer.dotnet.commands.test.splitTests

interface ListTestsOutputValueProcessor {
    fun process(output: String): ProcessedListTestsOutput
}


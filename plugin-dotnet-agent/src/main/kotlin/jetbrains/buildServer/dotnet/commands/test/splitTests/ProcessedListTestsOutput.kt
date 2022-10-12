package jetbrains.buildServer.dotnet.commands.test.splitTests

data class ProcessedListTestsOutput(
    val isValidIdentifier: Boolean,
    val value: String
)
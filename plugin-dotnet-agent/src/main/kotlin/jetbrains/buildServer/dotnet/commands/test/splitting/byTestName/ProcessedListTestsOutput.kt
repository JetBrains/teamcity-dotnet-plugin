

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

data class ProcessedListTestsOutput(
    val isValidIdentifier: Boolean,
    val value: String
)
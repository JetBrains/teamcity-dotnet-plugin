

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

interface ListTestsOutputValueProcessor {
    fun process(output: String): ProcessedListTestsOutput
}
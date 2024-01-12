

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

interface TestsSplittingByNamesReader {
    fun read(): Sequence<String>
}
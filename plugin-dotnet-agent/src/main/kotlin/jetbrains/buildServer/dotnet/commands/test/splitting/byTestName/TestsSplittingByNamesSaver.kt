

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

interface TestsSplittingByNamesSaver {
    fun tryToSave(presumablyTestNameLine: String)
}
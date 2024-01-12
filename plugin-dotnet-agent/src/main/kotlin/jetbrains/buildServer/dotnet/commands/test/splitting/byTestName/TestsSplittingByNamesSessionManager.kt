

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

interface TestsSplittingByNamesSessionManager {
    fun startSession() : TestsSplittingByNamesSession
}
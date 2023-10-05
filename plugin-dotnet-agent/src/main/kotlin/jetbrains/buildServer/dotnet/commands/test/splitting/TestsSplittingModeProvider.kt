package jetbrains.buildServer.dotnet.commands.test.splitting

import jetbrains.buildServer.agent.Version

interface TestsSplittingModeProvider {
    fun getMode(dotnetVersion: Version): TestsSplittingMode
}
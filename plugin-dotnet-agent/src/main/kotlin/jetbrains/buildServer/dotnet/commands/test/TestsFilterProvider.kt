

package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode

interface TestsFilterProvider {
    fun getFilterExpression(mode: TestsSplittingMode): String
}
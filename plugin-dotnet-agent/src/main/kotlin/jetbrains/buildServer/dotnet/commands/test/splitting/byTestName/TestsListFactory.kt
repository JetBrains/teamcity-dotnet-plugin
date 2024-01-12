

package jetbrains.buildServer.dotnet.commands.test.splitting.byTestName

interface TestsListFactory {
    fun new(): TestsList
}
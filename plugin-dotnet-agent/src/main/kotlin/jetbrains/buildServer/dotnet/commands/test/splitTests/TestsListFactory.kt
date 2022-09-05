package jetbrains.buildServer.dotnet.commands.test.splitTests

interface TestsListFactory {
    fun new(): TestsList
}
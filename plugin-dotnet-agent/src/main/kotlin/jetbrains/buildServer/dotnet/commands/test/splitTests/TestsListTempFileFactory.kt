package jetbrains.buildServer.dotnet.commands.test.splitTests

import jetbrains.buildServer.agent.runner.PathsService

class TestsListTempFileFactory(
    private val _pathsService: PathsService,
) : TestsListFactory {
    override fun new(): TestsList {
        val file = _pathsService.getTempFileName(".dotnet-tests-list");
        return TestsListTempFile(file)
    }
}
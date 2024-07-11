package jetbrains.buildServer.nunit

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.FileSystemService
import jetbrains.buildServer.HttpDownloader
import jetbrains.buildServer.tools.ServerToolProvider
import jetbrains.buildServer.tools.available.AvailableToolsFetcher
import jetbrains.buildServer.util.TimeService
import jetbrains.buildServer.web.functions.InternalProperties

class NUnitToolProviderInitializer(
    timeService: TimeService,
    availableToolsFetcher: AvailableToolsFetcher,
    httpDownloader: HttpDownloader,
    fileSystem: FileSystemService,
    extensionHolder: ExtensionHolder
) {
    init {
        if (InternalProperties.getBooleanOrTrue(NUnitRunnerConstants.NUNIT_RUNNER_ENABLED)) {
            val provider = NUnitToolProvider(timeService, availableToolsFetcher, httpDownloader, fileSystem)
            extensionHolder.registerExtension(ServerToolProvider::class.java, javaClass.name, provider)
        }
    }
}
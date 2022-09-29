package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.Serializer
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsProvider
import org.w3c.dom.Document

class TestRunSettingsFileNameProviderGenerated(
    private val _settingsProvider: TestRunSettingsProvider,
    private val _pathsService: PathsService,
    private val _fileSystem: FileSystemService,
    private val _serializer: Serializer<Document>,
) : TestRunSettingsFileProvider {
    override fun tryGet(command: DotnetCommandType) =
        try {
            _settingsProvider.tryCreate(command)
                ?.let { settings ->
                    _pathsService.getTempFileName(RunSettingsFileExtension)
                        .let { runSettingsFile ->
                            _fileSystem.write(runSettingsFile) { stream ->
                                _serializer.serialize(settings, stream)
                                runSettingsFile
                            }
                        }
                }
        }
        catch (error: Throwable) {
            LOG.error(error)
            null
        }

    companion object {
        private val LOG = Logger.getLogger(TestRunSettingsFileNameProviderGenerated::class.java)
        internal val RunSettingsFileExtension = ".runsettings"
    }
}
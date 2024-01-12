

package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.Deserializer
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsProvider
import org.w3c.dom.Document
import java.io.File

class TestRunSettingsExisting(
    private val _fileProviders: List<TestRunSettingsFileProvider>,
    private val _fileSystem: FileSystemService,
    private val _pathsService: PathsService,
    private val _deserializer: Deserializer<Document>,
    private val _xmlDocumentService: XmlDocumentService)
    : TestRunSettingsProvider {
    override fun tryCreate(context: DotnetCommandContext) =
            _fileProviders
                    .map { callOrDefault { it.tryGet(context) } }
                    .filter { it != null }
                    .map {
                        if(_fileSystem.isAbsolute(it!!))
                            it
                        else
                            File(_pathsService.getPath(PathType.WorkingDirectory), it.path)
                    }
                    .filter {
                        val exists = _fileSystem.isExists(it) && _fileSystem.isFile(it)
                        if (!exists)
                        {
                           LOG.warn("Cannot find settings file \"$it\".")
                        }

                        exists
                    }
                    .mapNotNull {
                        callOrDefault {
                            _fileSystem.read(it) {
                                _deserializer.deserialize(it)
                            }
                        }
                    }
                    .firstOrNull()
                    ?: callOrDefault { _xmlDocumentService.create() }

    private inline fun <T> callOrDefault(method: () -> T): T? {
        try {
            return method()
        }
        catch (error: Throwable) {
            LOG.error(error)
        }

        return null
    }

    companion object {
        private val LOG = Logger.getLogger(TestRunSettingsExisting::class.java)
    }
}
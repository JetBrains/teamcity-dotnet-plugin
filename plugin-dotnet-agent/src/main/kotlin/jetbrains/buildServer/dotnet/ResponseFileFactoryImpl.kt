package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.rx.use
import java.io.OutputStreamWriter

class ResponseFileFactoryImpl(
        private val _pathsService: PathsService,
        private val _argumentsService: ArgumentsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService,
        private val _virtualContext: VirtualContext)
    : ResponseFileFactory {
    override fun createResponeFile(description: String, arguments: Collection<CommandLineArgument>, verbosity: Verbosity?): Path {
        verbosity?.let {
            when (it) {
                Verbosity.Detailed, Verbosity.Diagnostic -> {
                    _loggerService.writeBlock("$BlockName $description".trim()).use {
                        for ((value) in arguments) {
                            _loggerService.writeStandardOutput(value, Color.Details)
                        }
                    }
                }
            }
        }

        val msBuildResponseFile = _pathsService.getTempFileName("$description$ResponseFileExtension")
        _fileSystemService.write(msBuildResponseFile) {
            OutputStreamWriter(it).use {
                for (line in arguments.map { _argumentsService.normalize(it.value) }) {
                    it.write("$line\n")
                }
            }
        }

        return Path(_virtualContext.resolvePath(msBuildResponseFile.path))
    }

    companion object {
        private val LOG = Logger.getLogger(ResponseFileFactoryImpl::class.java)

        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "Response File"
    }
}
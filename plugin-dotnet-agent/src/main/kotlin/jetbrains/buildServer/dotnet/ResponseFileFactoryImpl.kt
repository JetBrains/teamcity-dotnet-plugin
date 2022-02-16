package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.rx.use
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class ResponseFileFactoryImpl(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService,
        private val _msBuildParameterConverter: MSBuildParameterConverter,
        private val _virtualContext: VirtualContext)
    : ResponseFileFactory {
    override fun createResponeFile(
            description: String,
            arguments: Sequence<CommandLineArgument>,
            parameters: Sequence<MSBuildParameter>,
            verbosity: Verbosity?): Path {
        val args = (
                arguments
                + _msBuildParameterConverter.convert(parameters).map { CommandLineArgument(it) })
                .toList()

        verbosity?.let {
            when (it) {
                Verbosity.Detailed, Verbosity.Diagnostic -> {
                    _loggerService.writeBlock("$BlockName $description".trim()).use {
                        for ((value) in args) {
                            _loggerService.writeStandardOutput(value, Color.Details)
                        }
                    }
                }
                else -> { }
            }
        }

        val msBuildResponseFile = _pathsService.getTempFileName("$description$ResponseFileExtension")
        _fileSystemService.write(msBuildResponseFile) {
            // BOM
            it.write(BOM)
            OutputStreamWriter(it, StandardCharsets.UTF_8).use {
                for (arg in args) {
                    it.write(arg.value)
                    it.write("\n")
                }
            }
        }

        return Path(_virtualContext.resolvePath(msBuildResponseFile.path))
    }

    companion object {
        private val LOG = Logger.getLogger(ResponseFileFactoryImpl::class.java)
        internal val BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        internal const val ResponseFileExtension = ".rsp"
        internal const val BlockName = "Response File"
    }
}
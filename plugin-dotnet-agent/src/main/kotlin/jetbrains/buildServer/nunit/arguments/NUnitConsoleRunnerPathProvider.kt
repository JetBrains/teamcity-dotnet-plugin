package jetbrains.buildServer.nunit.arguments

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.nunit.NUnitRunnerConstants
import jetbrains.buildServer.nunit.NUnitSettings
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class NUnitConsoleRunnerPathProvider(
    private val _nUnitSettings: NUnitSettings,
    private val _fileSystem: FileSystemService,
    private val _pathsService: PathsService,
    private val _loggerService: LoggerService
) {
    val consoleRunnerPath: Path
        get() {
            val nUnitPathStr = _nUnitSettings.nUnitPath
                ?: throw RunBuildException("${NUnitRunnerConstants.NUNIT_PATH} is not defined")
            val nUnitPath = _pathsService.resolvePath(PathType.Checkout, nUnitPathStr)

            val checkedPaths = mutableListOf<Path>()
            val executable = getConsoleSearchPaths(nUnitPath).find {
                checkedPaths.add(it)
                _fileSystem.isFile(it.toFile())
            }

            if (executable != null) {
                _loggerService.writeDebug("Found NUnit Console executable at ${executable.absolutePathString()}")
                return executable
            }

            val errorMessage = buildString {
                appendLine("NUnit console tool was not found. Check path to NUnit console tool build step setting.")
                appendLine("""Current value: "$nUnitPathStr", used full path: "$nUnitPath"""")
                appendLine("Checked locations: ")
                checkedPaths.forEach { appendLine(it.absolutePathString()) }
            }
            _loggerService.writeDebug(errorMessage)
            throw RunBuildException(errorMessage)
        }

    private fun getConsoleSearchPaths(nUnitPath: Path) = buildList {
        add(nUnitPath)
        if (_fileSystem.isDirectory(nUnitPath.toFile())) {
            val binPath = nUnitPath.resolve("bin")
            val executable = "nunit3-console.exe"
            add(binPath.resolve("net35").resolve(executable))
            add(binPath.resolve("net20").resolve(executable))
            // for NUnit.Console since 3.16.0
            add(binPath.resolve(executable))
        }
    }
}
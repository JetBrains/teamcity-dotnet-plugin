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
    private val supportedNetFrameworkVersions = listOf("net462", "net35", "net20")
    private val nUnitConsoleExecutablePath = "nunit3-console.exe"

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
            addAll(supportedNetFrameworkVersions.map { binPath.resolve(it).resolve(nUnitConsoleExecutablePath) })
            // for NUnit.Console 3.16.0 (broken package structure)
            add(binPath.resolve(nUnitConsoleExecutablePath))
        }
    }
}
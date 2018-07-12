package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class VSTestLoggerEnvironmentAnalyzerImpl(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService)
    : VSTestLoggerEnvironmentAnalyzer {

    override fun analyze(targets: List<File>) {
        LOG.debug("Analyze targets to run tests")
        val checkoutDir = _pathsService.getPath(PathType.Checkout)
        val checkoutCanonical = checkoutDir.absoluteFile.canonicalPath
        val invalidTargets = mutableListOf<File>()
        val allTargets = targets.toMutableList()
        var useWorkingDirectory = false
        if (allTargets.isEmpty()) {
            allTargets.add(_pathsService.getPath(PathType.WorkingDirectory))
            useWorkingDirectory = true
        }

        for (target in allTargets) {
            if (_fileSystemService.isAbsolute(target)) {
                if (!target.absoluteFile.canonicalPath.startsWith(checkoutCanonical)) {
                    invalidTargets.add(target)
                    LOG.debug("\"$target\" is invalid to run tests")
                }

                continue
            }

            LOG.debug("\"$target\" is ok to run tests")
        }

        if (invalidTargets.isNotEmpty()) {
            val invalidTargetsList = invalidTargets.distinctBy { it.absolutePath }.joinToString(", ") { it.path }
            val targetType = if (useWorkingDirectory) "directory \"$invalidTargetsList\" is" else "file(s) \"$invalidTargetsList\" are"
            val warning = "The $targetType located outside of the build checkout directory: \"$checkoutDir\". In this case there can be problems with running this build tests on TeamCity agent. Please refer to this issue for details: https://youtrack.jetbrains.com/issue/TW-52485"
            LOG.warn(warning)
            _loggerService.writeErrorOutput(warning)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(VSTestLoggerEnvironmentAnalyzerImpl::class.java.name)
    }
}
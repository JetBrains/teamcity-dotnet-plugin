package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Logger
import java.io.File

class VisualStudioPackagesEnvironmentLocator(
        private val _environment: Environment)
    : VisualStudioPackagesLocator {

    override fun tryGetPackagesPath(): String? {
        val programDataPath = _environment.tryGetVariable("ProgramData")
        if (programDataPath.isNullOrBlank()) {
            LOG.debug("%ProgramData% directory not found.")
            return null
        }

        val packagesPath = File(programDataPath, VisualStudioPackagesPath)
        LOG.debug("Using Visual Studio packages cache directory \"$packagesPath\"");
        return packagesPath.path
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioPackagesEnvironmentLocator::class.java)
        private const val VisualStudioPackagesPath = "Microsoft/VisualStudio/Packages"
    }
}
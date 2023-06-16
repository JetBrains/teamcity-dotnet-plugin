package jetbrains.buildServer.dotnet.coverage

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.SimpleCommandLineProcessRunner
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

class DotnetCoverageReportGeneratorRunner(
    private val _params: DotnetCoverageParameters,
    private val _toolName: String,
    private val _coverToolExePath: File) {

    fun runReportGenerator(activity: String, arguments: List<String?>): Int {
        val cmdLine = GeneralCommandLine()
        cmdLine.exePath = _coverToolExePath.path
        cmdLine.addParameters(arguments)
        cmdLine.setEnvParams(_params.getBuildEnvironmentVariables())
        val logger: BuildProgressLogger = _params.getBuildLogger()

        //TODO: use jetbrains.buildServer.agent.BuildProcessFacade.createBuildRunnerContext instead
        logger.activityStarted(activity, ACTIVITY)
        return try {
            val result = SimpleCommandLineProcessRunner.runCommand(cmdLine, ByteArray(0))
            LOG.info("Started: " + cmdLine.commandLineString)
            logger.message("Started " + _toolName + ": " + cmdLine.commandLineString)
            logger.message("Output: " + result.stdout)
            if (result.stderr.trim { it <= ' ' }.length > 0) {
                logger.warning("Error: " + result.stderr)
            }
            val msg = _toolName + " exited with code: " + result.exitCode
            if (result.exitCode == 0) {
                logger.message(msg)
            } else {
                logger.warning(msg)
                logger.warning("$_toolName returned non-zero exit code.")
            }
            LOG.info("Exit Code: " + result.exitCode)
            result.exitCode
        } finally {
            logger.activityFinished(activity, ACTIVITY)
        }
    }

    companion object {
        private const val ACTIVITY = ".NET Coverage"
        private val LOG = Logger.getInstance(
            DotnetCoverageReportGeneratorRunner::class.java.name
        )
    }
}

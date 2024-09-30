package jetbrains.buildServer

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.serverSide.BuildServerAdapter
import jetbrains.buildServer.serverSide.BuildServerListener
import jetbrains.buildServer.serverSide.CurrentNodeInfo
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.executors.ExecutorServices
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.web.plugins.web.ModifiedPlugins
import java.io.File
import java.util.concurrent.RejectedExecutionException

class DotnetRunnersDeprecatedPluginInstaller(
    private val _server: SBuildServer,
    private val _plugins: ModifiedPlugins,
    private val _executors: ExecutorServices,
    eventDispatcher: EventDispatcher<BuildServerListener>
) {
    private val _deprecatedRunTypes = listOf("nunit")
    private val _pluginFileName = "dotNetRunners2.zip"

    init {
        eventDispatcher.addListener(object : BuildServerAdapter() {
            override fun serverStartup() {
                if (!isEnabled()) {
                    return
                }

                try {
                    _executors.lowPriorityExecutorService.submit {
                        try {
                            installPluginIfNeeded()
                        }
                        catch (e: Throwable) {
                            LOG.warnAndDebugDetails("An error occurred during changing parameters in SonarQube runner plugin", e);
                        }
                    }
                } catch (e: RejectedExecutionException) {
                    LOG.infoAndDebugDetails(
                        "Failed to start task to install deprecated DotnetRunners plugin: ${e.message}",
                        e
                    )
                }
            }
        })
    }

    fun installPluginIfNeeded() {
        if (_server.runTypeRegistry.registeredRunTypes.any { _deprecatedRunTypes.contains(it.type.lowercase()) }) {
            return
        }

        //DeprecatedPlugin::class.java.getResource("")
        val pluginFile = File("/Users/Vladislav.Ma-iu-shan/Downloads/dotNetRunners2.zip")
        if (!pluginFile.exists()) {
            return
        }

        if (!hasDeprecatedDotnetRunnerUsages()) {
            return
        }

        _plugins.install(_pluginFileName, pluginFile)

        // drop plugin zip
    }

    private fun isEnabled(): Boolean {
        if (!CurrentNodeInfo.isMainNode()) {
            return false
        }

        // add feature toggle

        return true
    }

    private fun hasDeprecatedDotnetRunnerUsages(): Boolean = _server.projectManager
        .activeBuildTypes
        .flatMap { it.resolvedSettings.buildRunners }
        .any { _deprecatedRunTypes.contains(it.type.lowercase()) }

    companion object {
        private val LOG: Logger = Logger.getInstance(DotnetRunnersDeprecatedPluginInstaller::class.java.name)
    }
}
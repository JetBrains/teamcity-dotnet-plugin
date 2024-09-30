package jetbrains.buildServer

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.executors.ExecutorServices
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.plugins.web.ModifiedPlugins
import java.io.File
import java.util.concurrent.RejectedExecutionException

class DotnetRunnersDeprecatedPluginInstaller(
    private val _server: SBuildServer,
    private val _plugins: ModifiedPlugins,
    private val _executors: ExecutorServices,
    private val _pluginDescriptor: PluginDescriptor,
    eventDispatcher: EventDispatcher<BuildServerListener>
) {
    companion object {
        private val LOG: Logger = Logger.getInstance(DotnetRunnersDeprecatedPluginInstaller::class.java.name)
        const val DOTNET_RUNNERS_INSTALL_ENABLED = "teamcity.internal.dotnet.runners.deprecated.install.enabled"
        const val DOTNET_RUNNERS_PLUGIN_FILE_NAME = "dotNetRunners/dotNetRunners.zip"
        private val DEPRECATED_RUN_TYPES = listOf(
            "nunit",
            "NAnt",
            "jetbrains.dotNetGenericRunner",
            "jetbrains.mspec",
            "MSBuild",
            "sln2003",
            "VS.Solution"
        )
    }

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
                        } catch (e: Throwable) {
                            LOG.warnAndDebugDetails(
                                "An error occurred during deprecated dotNetRunners plugin installation",
                                e
                            );
                        }
                    }
                } catch (e: RejectedExecutionException) {
                    LOG.warnAndDebugDetails(
                        "Failed to start task to install deprecated dotNetRunners plugin: ${e.message}",
                        e
                    )
                }
            }
        })
    }

    fun installPluginIfNeeded() {
        // runtypes are already installed
        if (_server.runTypeRegistry.registeredRunTypes.any { DEPRECATED_RUN_TYPES.contains(it.type.lowercase()) }) {
            LOG.info("Skipping deprecated dotNetRunners installation: deprecated runtypes are already registered")
            return
        }

        val pluginFile = getPluginFile()
        if (!pluginFile.exists()) {
            LOG.info("Skipping deprecated dotNetRunners installation: resource was not found")
            return
        }

        if (!hasDeprecatedDotnetRunnerUsages()) {
            LOG.info("Skipping deprecated dotNetRunners installation: corresponding build configurations are not found")
            return
        }

        _plugins.install(DOTNET_RUNNERS_PLUGIN_FILE_NAME, pluginFile)

        // TODO drop plugin zip
    }

    private fun isEnabled(): Boolean {
        if (!CurrentNodeInfo.isMainNode()) {
            LOG.info("Skipping deprecated dotNetRunners installation: not main node")
            return false
        }

        if (!TeamCityProperties.getBooleanOrTrue(DOTNET_RUNNERS_INSTALL_ENABLED)) {
            LOG.info("Skipping deprecated dotNetRunners installation: installation is disabled")
            return false
        }

        return true
    }

    private fun getPluginFile(): File {
        // return File("/Users/Vladislav.Ma-iu-shan/Downloads/dotNetRunners2.zip")
        val pluginRoot = _pluginDescriptor.pluginRoot;
        return File(pluginRoot, "server/dotNetRunners/dotNetRunners.zip")
    }

    private fun hasDeprecatedDotnetRunnerUsages(): Boolean {
        val buildRunner = _server.projectManager
            .activeBuildTypes
            .flatMap { it.resolvedSettings.buildRunners }
            .firstOrNull { r -> DEPRECATED_RUN_TYPES.any { it.equals(r.type, ignoreCase = true) } }

        if (buildRunner != null) {
            LOG.info("Will install deprecated dotNetRunners installation, found ${buildRunner.name} configuration with type ${buildRunner.type}")
            return true
        }

        return false
    }
}
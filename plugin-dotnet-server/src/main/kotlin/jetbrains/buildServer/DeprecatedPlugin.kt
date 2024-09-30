package jetbrains.buildServer

import jetbrains.buildServer.serverSide.BuildServerAdapter
import jetbrains.buildServer.serverSide.BuildServerListener
import jetbrains.buildServer.serverSide.CurrentNodeInfo
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.web.plugins.web.ModifiedPlugins
import java.io.File

// main node?
// docker
// check metarunners
// where to place?
// use executor to make it async?
class DeprecatedPlugin(
    private val _server: SBuildServer,
    private val _plugins: ModifiedPlugins,
    eventDispatcher: EventDispatcher<BuildServerListener>
) {
    private val _deprecatedRunTypes = listOf("nunit")
    private val _pluginFileName = "dotNetRunners2.zip"

    init {
        eventDispatcher.addListener(object : BuildServerAdapter() {
            // TODO move to different executor?
            override fun serverStartup() = installPluginIfNeeded()
        })
    }

    fun installPluginIfNeeded() {
        if (!CurrentNodeInfo.isMainNode()) {
            return
        }

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

    private fun hasDeprecatedDotnetRunnerUsages(): Boolean {
        val kek = _server.projectManager.activeBuildTypes[0].buildRunners[0]


        return _server.projectManager
            .activeBuildTypes
            .flatMap { it.resolvedSettings.buildRunners }
            .any { _deprecatedRunTypes.contains(it.type.lowercase()) }
    }
}
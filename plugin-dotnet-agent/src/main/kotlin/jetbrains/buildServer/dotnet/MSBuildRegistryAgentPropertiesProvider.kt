package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_MSBUILD_TOOLS
import jetbrains.buildServer.agent.Logger
import java.io.File

class MSBuildRegistryAgentPropertiesProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _msuildValidator: MSBuildValidator)
    : AgentPropertiesProvider {

    override val desription = "MSBuild in registry"

    override val properties: Sequence<AgentProperty> get()  {
        val props = mutableListOf<AgentProperty>()

        for (key in RegKeys) {
            _windowsRegistry.accept(
                    key,
                    object: WindowsRegistryVisitor {
                        override fun visit(key: WindowsRegistryKey) = true
                        override fun visit(value: WindowsRegistryValue): Boolean {
                            LOG.debug("Visit ${value}")
                            if (
                                    value.type == WindowsRegistryValueType.Str
                                    && value.text.isNotBlank()
                                    && "MSBuildToolsPath".equals(value.key.parts.lastOrNull(), true)) {
                                val versionStr = value.key.parts.dropLast(1).lastOrNull()
                                LOG.debug("Version: $versionStr")
                                versionStr?.let { version ->
                                    val path = File(value.text)
                                    LOG.debug("Path: $path")
                                    if (_msuildValidator.isValid(path)) {
                                        var property = AgentProperty(ToolInstanceType.MSBuildTool, "$CONFIG_PREFIX_MSBUILD_TOOLS${version}_${value.key.bitness.platform.id}_Path", path.path)
                                        props.add(property)
                                        LOG.debug("Add property: $path")
                                    } else {
                                        LOG.warn("Cannot find MSBuild in \"${value.text}\".")
                                    }
                                }
                            }

                            return true
                        }
                    },
            true)
        }

        return props.asSequence()
    }

    companion object {
        private val LOG = Logger.getLogger(MSBuildRegistryAgentPropertiesProvider::class.java)

        private val RegKeys = WindowsRegistryBitness.values().map {
            WindowsRegistryKey.create(
                    it,
                    WindowsRegistryHive.LOCAL_MACHINE,
                    "SOFTWARE",
                    "Microsoft",
                    "MSBuild",
                    "ToolsVersions")
        }
    }
}
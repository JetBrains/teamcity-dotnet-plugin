package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.AgentPropertyType
import org.apache.log4j.Logger
import java.io.File

class MSBuildRegistryAgentPropertiesProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _msuildValidator: MSBuildValidator)
    : AgentPropertiesProvider {

    override val desription = "MSBuild in registry"

    override val properties: Sequence<AgentProperty> get()  {
        val props = mutableListOf<AgentProperty>()

        for (key in RegKeys) {
            _windowsRegistry.get(key, object: WindowsRegistryVisitor {
                override fun accept(key: WindowsRegistryKey) = true
                override fun accept(value: WindowsRegistryValue): Boolean {
                    if (
                            value.type == WindowsRegistryValueType.Str
                            && value.text.isNotBlank()
                            && "MSBuildToolsPath".equals(value.key.parts.lastOrNull(), true)) {
                        val versionStr = value.key.parts.dropLast(1).lastOrNull()
                        versionStr?.let { version ->
                            val path = File(value.text)
                            if (_msuildValidator.isValide(path)) {
                                props.add(AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools${version}_${value.key.bitness.platform.id}_Path", path.path))
                            } else {
                                LOG.warn("Cannot find MSBuild in \"${value.text}\".")
                            }
                        }
                    }

                    return true
                }
            })
        }

        return props.asSequence()
    }

    companion object {
        private val LOG = Logger.getLogger(MSBuildRegistryAgentPropertiesProvider::class.java)

        private val RegKeys = sequenceOf<WindowsRegistryKey>(
                WindowsRegistryKey.create(
                        WindowsRegistryBitness.Bitness64,
                        WindowsRegistryHive.LOCAL_MACHINE,
                        "SOFTWARE",
                        "Microsoft",
                        "MSBuild",
                        "ToolsVersions"),
                WindowsRegistryKey.create(
                        WindowsRegistryBitness.Bitness32,
                        WindowsRegistryHive.LOCAL_MACHINE,
                        "SOFTWARE",
                        "Microsoft",
                        "MSBuild",
                        "ToolsVersions")
        )
    }
}
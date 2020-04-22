package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.Bitness
import java.io.File

class MSBuildRegistryAgentPropertiesProvider(
        private val _windowsRegistry: WindowsRegistry)
    : AgentPropertiesProvider {

    override val desription = "MSBuild in registry"

    override val properties: Sequence<AgentProperty> get()  {
        val props = mutableListOf<AgentProperty>()

        for (key in RegKeys) {
            _windowsRegistry.get(key, object: WindowsRegistryVisitor {
                override fun accept(key: WindowsRegistryKey) = Unit
                override fun accept(value: WindowsRegistryValue) {
                    if (
                            value.type == WindowsRegistryValueType.Str
                            && value.text.isNotBlank()
                            && "MSBuildToolsPath".equals(value.key.parts.lastOrNull(), true)) {
                        val versionStr = value.key.parts.dropLast(1).lastOrNull()
                        versionStr?.let { version ->
                            props.add(AgentProperty("MSBuildTools${version}_${value.key.bitness.platform.id}_Path", value.text))
                        }
                    }
                }
            } )
        }

        return props.asSequence()
    }

    companion object {
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
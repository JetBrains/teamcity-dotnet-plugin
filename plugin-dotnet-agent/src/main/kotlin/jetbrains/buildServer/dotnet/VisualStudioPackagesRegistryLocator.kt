package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import org.apache.log4j.Logger
import java.io.File

class VisualStudioPackagesRegistryLocator(
        private val _windowsRegistry: WindowsRegistry)
    : VisualStudioPackagesLocator {

    override fun tryGetPackagesPath(): String? {
        var packagesPath: String? = null;
        for (key in RegKeys) {
            _windowsRegistry.get(
                    key,
                    object : WindowsRegistryVisitor {
                        override fun accept(key: WindowsRegistryKey) = false
                        override fun accept(value: WindowsRegistryValue): Boolean {
                            if (value.type == WindowsRegistryValueType.Str && "CachePath".equals(value.key.parts.lastOrNull(), true)) {
                                packagesPath = value.text
                                LOG.debug("Using Visual Studio packages cache directory \"$packagesPath\"");
                                return false
                            }

                            return true
                        }
                    },
                    false)
        }

        return packagesPath
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioPackagesRegistryLocator::class.java)

        private val RegKeys = sequenceOf<WindowsRegistryKey>(
                WindowsRegistryKey.create(
                        WindowsRegistryBitness.Bitness64,
                        WindowsRegistryHive.LOCAL_MACHINE,
                        "SOFTWARE",
                        "Microsoft",
                        "VisualStudio",
                        "Setup"),
                WindowsRegistryKey.create(
                        WindowsRegistryBitness.Bitness32,
                        WindowsRegistryHive.LOCAL_MACHINE,
                        "SOFTWARE",
                        "Microsoft",
                        "VisualStudio",
                        "Setup")
        )
    }
}
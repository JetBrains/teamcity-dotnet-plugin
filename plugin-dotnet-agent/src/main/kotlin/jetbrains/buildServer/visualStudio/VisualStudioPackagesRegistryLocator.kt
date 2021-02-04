package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import org.apache.log4j.Logger

class VisualStudioPackagesRegistryLocator(
        private val _windowsRegistry: WindowsRegistry)
    : VisualStudioPackagesLocator {

    override fun tryGetPackagesPath(): String? {
        var packagesPath: String? = null;
        for (key in RegKeys) {
            _windowsRegistry.accept(
                    key,
                    object : WindowsRegistryVisitor {
                        override fun visit(key: WindowsRegistryKey) = false
                        override fun visit(value: WindowsRegistryValue): Boolean {
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

        internal val RegKeys = sequenceOf<WindowsRegistryKey>(
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
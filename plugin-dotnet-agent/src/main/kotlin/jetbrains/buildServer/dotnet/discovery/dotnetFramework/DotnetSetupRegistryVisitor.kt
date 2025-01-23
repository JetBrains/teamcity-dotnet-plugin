package jetbrains.buildServer.dotnet.discovery.dotnetFramework

import WindowsRegistryValueType
import jetbrains.buildServer.agent.*
import java.io.File

class DotnetSetupRegistryVisitor(
    private val _environment: DotnetFrameworksEnvironment
) : DotnetFrameworksWindowsRegistryVisitor {

    private val _frameworks = mutableMapOf<String, Framework>()

    override val keys = Keys

    override fun getFrameworks() = sequence {
        for (framework in _frameworks.values.filter { !it.isEmpty }) {
            val frameworkBasePath = framework.path
            if (frameworkBasePath != null) {
                var version: Version
                if (framework.release.major == framework.version.major
                    && framework.release.minor == framework.version.minor
                    && (framework.release.patch == framework.version.patch || framework.release.patch == 0)
                ) {
                    version = framework.version
                } else {
                    version = framework.release
                    if (version == Version.Empty) {
                        version = framework.version
                    }
                }

                if (version != Version.Empty) {
                    yield(DotnetFramework(framework.bitness.getPlatform(isArm = false), version, frameworkBasePath))

                    val armFrameworkPath = getArmFrameworkPath(framework, frameworkBasePath)
                    if(armFrameworkPath != null) {
                        yield(DotnetFramework(framework.bitness.getPlatform(isArm = true), version, armFrameworkPath))
                    }
                }
            }
        }

        _frameworks.clear()
    }

    private fun getArmFrameworkPath(
        framework: Framework,
        frameworkBasePath: File
    ): File? {
        if (framework.release < ArmSupportSince) return null

        return _environment.tryGetRoot(framework.bitness, isArm = true)?.let { installRootArm64 ->
            // installRootArm64 provides the base path to the ARM installation
            // Example: C:\Windows\Microsoft.NET\FrameworkArm64
            // It does not include the specific framework version directory
            // Example of full path: C:\Windows\Microsoft.NET\FrameworkArm64\v4.0.30319

            // Extract the CLR Runtime version from the provided x64 installation path (frameworkBasePath)
            val clrRuntimeVersion = frameworkBasePath.name // e.g. v4.0.30319
            return installRootArm64.resolve(clrRuntimeVersion)
        }
    }

    override fun visit(key: WindowsRegistryKey) = true

    override fun visit(value: WindowsRegistryValue): Boolean {
        val bitness = value.key.bitness
        if (value.key.parts.size == Deep + 2) {
            val subKey = value.key.parts.takeLast(2)
            val key = subKey[0].lowercase()
            val name = subKey[1].lowercase()
            if (value.type == WindowsRegistryValueType.Str && value.text.isNotBlank() && key.startsWith("v")) {
                when (key) {
                    "v3.0" -> {
                        when (name) {
                            VersionName -> {
                                val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                                framework.version = Version.parse(value.text)
                                _environment.tryGetRoot(value.key.bitness)?.let { installRoot ->
                                    framework.path = File(installRoot, key)
                                }
                            }
                        }
                    }

                    "v3.5" -> {
                        when (name) {
                            VersionName -> {
                                val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                                framework.version = Version.parse(value.text)
                                if (framework.path == null) {
                                    _environment.tryGetRoot(value.key.bitness)?.let { installRoot ->
                                        framework.path = File(installRoot, key)
                                    }
                                }
                            }

                            InstallPathName -> {
                                val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                                framework.path = if (value.text.isNotBlank()) File(value.text) else null
                            }
                        }
                    }
                }
            }
        }

        if (value.key.parts.size == Deep + 3) {
            val subKey = value.key.parts.takeLast(3)
            val key = subKey[0].lowercase()
            val versionType = subKey[1]
            val name = subKey[2]
            if (key.equals("v4") && versionType.equals("Full", true)) {
                when (name.lowercase()) {
                    VersionName -> {
                        if (value.type == WindowsRegistryValueType.Str && value.text.isNotBlank()) {
                            val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                            framework.version = Version.parse(value.text)
                        }
                    }

                    InstallPathName -> {
                        if (value.type == WindowsRegistryValueType.Str && value.text.isNotBlank()) {
                            val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                            framework.path = if (value.text.isNotBlank()) File(value.text) else null
                        }
                    }

                    ReleaseName -> {
                        if (value.type == WindowsRegistryValueType.Int && value.number != 0L) {
                            val framework = _frameworks.getOrPut(key) { Framework(bitness) }
                            framework.release = getFrameworkVersion(value.number)
                        }
                    }
                }
            }
        }

        return true
    }

    companion object {
        val InstallPathName = "installpath"
        val VersionName = "version"
        val ReleaseName = "release"
        val ArmSupportSince = Version(4, 8, 1)

        val Keys: Sequence<WindowsRegistryKey>
            get() =
                WindowsRegistryBitness
                    .values()
                    .asSequence()
                    .map {
                        WindowsRegistryKey.create(
                            it,
                            WindowsRegistryHive.LOCAL_MACHINE,
                            "SOFTWARE",
                            "Microsoft",
                            "NET Framework Setup",
                            "NDP"
                        )
                    }

        private val Deep = DotnetPolicyRegistryVisitor.Keys.first().parts.size

        // https://learn.microsoft.com/en-us/dotnet/framework/migration-guide/how-to-determine-which-versions-are-installed#minimum-version
        private fun getFrameworkVersion(releaseKey: Long) = when {
            releaseKey >= 533320 -> Version(4, 8, 1)
            releaseKey >= 528040 -> Version(4, 8)
            releaseKey >= 461808 -> Version(4, 7, 2)
            releaseKey >= 461308 -> Version(4, 7, 1)
            releaseKey >= 460798 -> Version(4, 7)
            releaseKey >= 394802 -> Version(4, 6, 2)
            releaseKey >= 394254 -> Version(4, 6, 1)
            releaseKey >= 393295 -> Version(4, 6)
            releaseKey >= 379893 -> Version(4, 5, 2)
            releaseKey >= 378675 -> Version(4, 5, 1)
            releaseKey >= 378389 -> Version(4, 5)
            else -> Version.Empty
        }
    }

    private data class Framework(
        val bitness: WindowsRegistryBitness,
        var version: Version = Version.Empty,
        var release: Version = Version.Empty,
        var path: File? = null
    ) {
        val isEmpty get() = !((version != Version.Empty || release != Version.Empty) && path != null)
    }
}
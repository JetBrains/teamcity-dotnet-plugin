package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.*

// https://docs.microsoft.com/ru-ru/dotnet/standard/frameworks#net-5-os-specific-tfms
// https://docs.microsoft.com/ru-ru/dotnet/standard/net-standard

class SdkResolverImpl(
        private val _sdkTypeResolver: SdkTypeResolver)
    : SdkResolver {
    override fun resolveSdkVersions(framework: Framework, propeties: Collection<Property>) =
        resolveSdkVersions(framework).map { if(it.version.getPart(0) != 4) SdkVersion(it.version.trim(), it.sdkType, it.versionType) else it }

    override fun getCompatibleVersions(version: Version): Sequence<SdkVersion> =
            when(val sdkType = _sdkTypeResolver.tryResolve(version)) {
                SdkType.Dotnet, SdkType.DotnetCore -> getDotnetVersions(SdkVersion(version, sdkType, SdkVersionType.Default))
                SdkType.DotnetFramework, SdkType.FullDotnetTargetingPack -> getFullDotnetVersion(SdkVersion(version, sdkType, SdkVersionType.Default))
                else -> emptySequence()
            }

    private fun resolveSdkVersions(framework: Framework) = sequence<SdkVersion> {
        FrameworkRegex.matchEntire(framework.name)?.let {
            val name = it.groupValues[1].toLowerCase()
            var versionStr = it.groupValues[2]
            // val os = it.groupValues[3].toLowerCase()

            if (!versionStr.contains('.')) {
                versionStr = versionStr.map { "$it" }.joinToString(".")
            }

            Version.tryParse(versionStr)?.let {
                version ->
                when {
                    // net5.0, net6.0
                    name == "net" && version >= Version(5, 0) -> {
                        yieldAll(getDotnetVersions(SdkVersion(version, SdkType.Dotnet, SdkVersionType.Default)))
                    }

                    // netcoreapp1.0 - netcoreapp3.1
                    name == "netcoreapp" -> {
                        yieldAll(getDotnetVersions(SdkVersion(version, SdkType.DotnetCore, SdkVersionType.Default)))
                    }

                    name == "net" && version >= Version(3, 5) -> {
                        yield(SdkVersion(version, SdkType.FullDotnetTargetingPack, SdkVersionType.Default))
                    }

                    name == "netstandard" -> {
                        when {
                            version >= Version(2, 1) -> {
                            }

                            version >= Version(1, 4) -> {
                                yieldAll(getFullDotnetVersion(SdkVersion(Version(4, 6, 1), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)))
                            }

                            version == Version(1, 3) -> {
                                yieldAll(getFullDotnetVersion(SdkVersion(Version(4, 6), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)))
                            }

                            version == Version(1, 2) -> {
                                yieldAll(getFullDotnetVersion(SdkVersion(Version(4, 5, 1), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)))
                            }

                            else -> {
                                yieldAll(getFullDotnetVersion(SdkVersion(Version(4, 5), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)))
                            }
                        }

                        when {
                            version >= Version(2, 1) -> {
                                yieldAll(getDotnetVersions(SdkVersion(Version(3, 0), SdkType.DotnetCore, SdkVersionType.Default)))
                            }

                            version >= Version(2, 0) -> {
                                yieldAll(getDotnetVersions(SdkVersion(Version(2, 0), SdkType.DotnetCore, SdkVersionType.Default)))
                            }

                            else -> {
                                yieldAll(getDotnetVersions(SdkVersion(Version(1, 0), SdkType.DotnetCore, SdkVersionType.Default)))
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val FrameworkRegex = Regex("^([a-z]+)([\\d.]+)(.*)$", RegexOption.IGNORE_CASE)

        private fun getDotnetVersions(minimalVersion: SdkVersion) = sequence {
            val versions = WellknownDotnetVersions
                    .filter { it.version > minimalVersion.version }

            when {
                !versions.any() -> {
                    yield(minimalVersion)
                }
                else -> {
                    yieldAll((versions.map {
                        if(minimalVersion.version.size > 1 && (it.version.getPart(0) == minimalVersion.version.getPart(0)))
                        {
                            it
                        }
                        else {
                            SdkVersion(Version(it.version.getPart(0)), it.sdkType, SdkVersionType.Compatible)
                        }
                    } + minimalVersion).reversed().distinctBy { it.version }.reversed())
                }
            }
        }

        private fun getFullDotnetVersion(minimalVersion: SdkVersion) = sequence {
            val versions = WellknownFullDotnetVersions
                    .filter { it > minimalVersion.version }

            when {
                minimalVersion.version < Version(4, 5) -> {
                    yield(minimalVersion)
                }
                !versions.any() -> {
                    yield(minimalVersion)
                }
                else -> {
                    yieldAll((versions.map {
                        if(minimalVersion.version.size > 2 && (it.getPart(0) == minimalVersion.version.getPart(0) && it.getPart(1) == minimalVersion.version.getPart(1)))
                        {
                            SdkVersion(it, SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)
                        }
                        else {
                            SdkVersion(Version(it.getPart(0), it.getPart(1)), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)
                        }
                    } + sequenceOf(minimalVersion)).reversed().distinctBy { it.version }.reversed())
                }
            }
        }

        private val WellknownDotnetVersions = listOf(
                SdkVersion(Version(6, 0), SdkType.Dotnet, SdkVersionType.Compatible),
                SdkVersion(Version(5, 0), SdkType.Dotnet, SdkVersionType.Compatible),
                SdkVersion(Version(3, 1), SdkType.DotnetCore, SdkVersionType.Compatible),
                SdkVersion(Version(3, 0), SdkType.DotnetCore, SdkVersionType.Compatible),
                SdkVersion(Version(2, 2), SdkType.DotnetCore, SdkVersionType.Compatible),
                SdkVersion(Version(2, 1), SdkType.DotnetCore, SdkVersionType.Compatible),
                SdkVersion(Version(2, 0), SdkType.DotnetCore, SdkVersionType.Compatible),
                SdkVersion(Version(1, 1), SdkType.DotnetCore, SdkVersionType.Compatible),
                SdkVersion(Version(1, 0), SdkType.DotnetCore, SdkVersionType.Compatible)
        )

        private val WellknownFullDotnetVersions = listOf(
                Version(4, 8),
                Version(4, 7, 2),
                Version(4, 7, 1),
                Version(4, 7),
                Version(4, 6, 2),
                Version(4, 6, 1),
                Version(4, 6),
                Version(4, 5, 2),
                Version(4, 5, 1),
                Version(4, 5)
        )
    }
}
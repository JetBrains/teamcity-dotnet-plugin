package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.dotnet.*
import java.util.*

// https://docs.microsoft.com/ru-ru/dotnet/standard/frameworks#net-5-os-specific-tfms
// https://docs.microsoft.com/ru-ru/dotnet/standard/net-standard

class SdkResolverImpl() : SdkResolver {
    override fun resolveSdkVersions(framework: Framework, propeties: Collection<Property>) =
        resolveSdkVersions(framework).map { it.trim() }

    private fun resolveSdkVersions(framework: Framework) = sequence {
        FrameworkRegex.matchEntire(framework.name)?.let {
            val name = it.groupValues[1].toLowerCase()
            var versionStr = it.groupValues[2]
            val os = it.groupValues[3].toLowerCase()

            if (!versionStr.contains('.')) {
                versionStr = versionStr.map { "$it" }.joinToString(".")
            }

            Version.tryParse(versionStr)?.let {
                version ->
                when {
                    // net5.0, net6.0
                    name == "net" && version >= Version(5, 0) -> {
                        yieldAll(getDotnetVersions(version))
                    }

                    // netcoreapp1.0 - netcoreapp3.1
                    name == "netcoreapp" -> {
                        yieldAll(getDotnetVersions(version))
                    }

                    name == "net" && version >= Version(3, 5) -> {
                        yieldAll(getFullDotnetVersion(version))
                    }

                    name == "netstandard" -> {
                        when {
                            version >= Version(2, 1) -> {
                                yieldAll(getDotnetVersions(Version(3, 0)))
                            }

                            version >= Version(2, 0) -> {
                                yieldAll(getDotnetVersions(Version(2)))
                            }

                            else -> {
                                yieldAll(getDotnetVersions(Version(1)))
                            }
                        }

                        when {
                            version >= Version(2, 1) -> {
                            }

                            version >= Version(1, 4) -> {
                                yieldAll(getFullDotnetVersion(Version(4, 6, 1)))
                            }

                            version == Version(1, 3) -> {
                                yieldAll(getFullDotnetVersion(Version(4, 6)))
                            }

                            version == Version(1, 2) -> {
                                yieldAll(getFullDotnetVersion(Version(4, 5, 1)))
                            }

                            else -> {
                                yieldAll(getFullDotnetVersion(Version(4, 5)))
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val FrameworkRegex = Regex("^([a-z]+)([\\d.]+)(.*)$", RegexOption.IGNORE_CASE)

        private fun getDotnetVersions(minimalVersion: Version) = sequence<Version> {
            val versions = WellknownDotnetVersions
                    .filter { it > minimalVersion }

            when {
                !versions.any() -> {
                    yield(minimalVersion)
                }
                else -> {
                    yieldAll((versions.map {
                        if(minimalVersion.size > 1 && (it.getPart(0) == minimalVersion.getPart(0)))
                        {
                            it
                        }
                        else {
                            Version(it.getPart(0))
                        }
                    } + minimalVersion).distinct())
                }
            }
        }

        private fun getFullDotnetVersion(minimalVersion: Version) = sequence<Version> {
            val versions = WellknownFullDotnetVersions
                    .filter { it > minimalVersion }

            when {
                minimalVersion < Version(4, 5) -> {
                    yield(minimalVersion)
                }
                !versions.any() -> {
                    yield(minimalVersion)
                }
                else -> {
                    yieldAll((versions.map {
                        if(minimalVersion.size > 2 && (it.getPart(0) == minimalVersion.getPart(0) && it.getPart(1) == minimalVersion.getPart(1)))
                        {
                            it
                        }
                        else {
                            Version(it.getPart(0), it.getPart(1))
                        }
                    } + sequenceOf(minimalVersion)).distinct())
                }
            }
        }

        private val WellknownDotnetVersions = listOf<Version>(
                Version(5, 0),
                Version(3, 1),
                Version(3, 0),
                Version(2, 2),
                Version(2, 1),
                Version(2, 0),
                Version(1, 1),
                Version(1, 0)
        )


        private val WellknownFullDotnetVersions = listOf<Version>(
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
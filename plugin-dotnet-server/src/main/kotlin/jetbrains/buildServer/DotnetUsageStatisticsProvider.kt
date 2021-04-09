package jetbrains.buildServer

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_COMMAND
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_MSBUILD_VERSION
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_VISUAL_STUDIO_VERSION
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.usageStatistics.UsageStatisticsPublisher
import jetbrains.buildServer.usageStatistics.impl.providers.BaseDefaultUsageStatisticsProvider
import jetbrains.buildServer.usageStatistics.presentation.UsageStatisticsPresentationManager
import jetbrains.buildServer.usageStatistics.presentation.formatters.PercentageFormatter
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint

class DotnetUsageStatisticsProvider(
        private val _server: SBuildServer)
    : BaseDefaultUsageStatisticsProvider() {
    init {
        myGroupName = GROUP_NAME
    }

    override fun getGroupPosition() = DOTNET_GROUP

    override fun accept(publisher: UsageStatisticsPublisher, presentationManager: UsageStatisticsPresentationManager) {
        val statistics = _server
                .getProjectManager()
                .getActiveBuildTypes()
                .map { it.resolvedSettings.buildRunners }
                .flatten()
                .groupBy { it.type }
                .map {
                    when (it.key) {
                        DotnetConstants.RUNNER_TYPE -> it.value.toDotnetStat()
                        "MSBuild" -> it.value.toMSBuildStat()
                        "VS.Solution" -> it.value.toVSStat()
                        "VisualStudioTest" -> it.value.toVSTestStat()
                        "jb.nuget.installer" -> it.value.toStat { NugetInstallerToolDesc }
                        "jb.nuget.publish" -> it.value.toStat { NugetPublishToolDesc }
                        "jb.nuget.pack" -> it.value.toStat { NugetPackToolDesc }
                        else -> emptyList()
                    }
                }
                .flatten()

        val formatter = PercentageFormatter(statistics.filter { !it.descriptor.isAdditionalCase }.sumBy { it.count })

        for (stat in statistics) {
            publisher.publishStatistic(stat.descriptor.id, stat.count)
            presentationManager.applyPresentation(stat.descriptor.id, stat.descriptor.description, myGroupName, formatter, null)
        }
    }

    private fun List<SBuildRunnerDescriptor>.toDotnetStat() =
            this.toStat {
                sequence {
                    val runner = it
                    var command = runner.parameters[PARAM_COMMAND]
                    val tool = when (command) {
                        "devenv" -> runner.parameters[PARAM_VISUAL_STUDIO_VERSION] ?: DefaultVSVersion
                        "vstest" -> runner.parameters[PARAM_VISUAL_STUDIO_VERSION] ?: DefaultVSTestVersion
                        "msbuild" -> runner.parameters[PARAM_MSBUILD_VERSION] ?: DefaultMSBuildVersion
                        else -> null
                    }?.let {
                        Tool.tryParse(it)
                    }

                    if (command == "-") {
                        command = "custom"
                    }

                    runner.parameters["plugin.docker.imageId"]?.let {
                        val dockerPaltform = runner.parameters["plugin.docker.imagePlatform"]?.let { " $it " } ?: " "
                        val dockerId = "_${dockerPaltform.trim()}docker"
                        when {
                            tool != null -> yield(ToolDescriptor("${tool.id}$dockerId", ".NET: ${tool.description} in${dockerPaltform}docker container", true))
                            command != null -> yield(ToolDescriptor("dotnet_$command$dockerId", ".NET: $command in${dockerPaltform}docker container", true))
                        }

                    }

                    when {
                        tool != null -> yield(ToolDescriptor(tool.id, ".NET: ${tool.description}"))
                        command != null -> yield(ToolDescriptor("dotnet_$command", ".NET: $command"))
                    }
                }
            }

    private fun List<SBuildRunnerDescriptor>.toMSBuildStat() =
            this.toStat {
                sequence {
                    val params = it.parameters
                    val msbuildVersion = params["msbuild_version"]
                    msbuildVersion?.let {
                        when (it) {
                            "16.0" -> "Build Tools 2019"
                            "15.0" -> "Build Tools 2017"
                            "14.0" -> "Build Tools 2015"
                            "12.0" -> "Build Tools 2013"
                            "4.5" -> "Framework 4.5"
                            "4.0" -> "Framework 4.0"
                            "3.5" -> "Framework 3.5"
                            "2.0" -> "Framework 2.0"
                            "mono_4.5" -> "Mono xbuild 4.5"
                            "mono_4.0" -> "Mono xbuild 4.0"
                            "mono_3.5" -> "Mono xbuild 3.5"
                            "mono" -> "Mono xbuild 2.0"
                            else -> it
                        }
                    }?.let {
                        val platform = params["run-platform"]
                        val id = "MSBuild${msbuildVersion}${platform ?: ""}".trim()
                        yield(ToolDescriptor(id, "$MSBUILD_RUNNER: ${it} ${platform ?: ""}".trim()))
                    }

                }
            }

    private fun List<SBuildRunnerDescriptor>.toVSStat() =
            this.toStat {
                sequence {
                    it.parameters["vs.version"]?.let {
                        when (it) {
                            "vs2019" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2019"))
                            "vs2017" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2017"))
                            "vs2015" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2015"))
                            "vs2013" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2013"))
                            "vs2012" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2012"))
                            "vs2010" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2010"))
                            "vs2008" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2008"))
                            "vs2005" -> yield(ToolDescriptor(it, "$VS_RUNNER: 2005"))
                            else -> yield(ToolDescriptor(it, it))
                        }
                    }
                }
            }

    private fun List<SBuildRunnerDescriptor>.toVSTestStat() =
            this.toStat {
                sequence {
                    val params = it.parameters
                    params["vstest_engine"]?.let {
                        when (it) {
                            "MSTest" -> params["vstest_runner_path"]?.let {
                                when (it) {
                                    "%teamcity.dotnet.mstest.16.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2019"))
                                    "%teamcity.dotnet.mstest.15.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2017"))
                                    "%teamcity.dotnet.mstest.14.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2015"))
                                    "%teamcity.dotnet.mstest.12.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2013"))
                                    "%teamcity.dotnet.mstest.11.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2012"))
                                    "%teamcity.dotnet.mstest.10.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2010"))
                                    "%teamcity.dotnet.mstest.9.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2008"))
                                    "%teamcity.dotnet.mstest.8.0%" -> yield(ToolDescriptor(it, "$MSTESTS_RUNNER: 2005"))
                                    else -> yield(ToolDescriptor("%teamcity.dotnet.mstest%", "$MSTESTS_RUNNER: Custom"))
                                }
                            }
                            "VSTest" -> params["vstest_runner_path"]?.let {
                                when (it) {
                                    "%teamcity.dotnet.vstest.16.0%" -> yield(ToolDescriptor(it, "$VSTESTS_RUNNER: 2019"))
                                    "%teamcity.dotnet.vstest.15.0%" -> yield(ToolDescriptor(it, "$VSTESTS_RUNNER: 2017"))
                                    "%teamcity.dotnet.vstest.14.0%" -> yield(ToolDescriptor(it, "$VSTESTS_RUNNER: 2015"))
                                    "%teamcity.dotnet.vstest.12.0%" -> yield(ToolDescriptor(it, "$VSTESTS_RUNNER: 2013"))
                                    "%teamcity.dotnet.vstest.11.0%" -> yield(ToolDescriptor(it, "$VSTESTS_RUNNER: 2012"))
                                    else -> yield(ToolDescriptor("%teamcity.dotnet.vstest%", "$VSTESTS_RUNNER: Custom"))
                                }
                            }
                            else -> yield(ToolDescriptor("%teamcity.dotnet.vstest.unknown%", "Custom"))
                        }
                    }
                }
            }

    private fun List<SBuildRunnerDescriptor>.toStat(toolDescriptorsFactory: (SBuildRunnerDescriptor) -> Sequence<ToolDescriptor>) =
            this.asSequence()
                    .flatMap { toolDescriptorsFactory(it) }
                    .groupBy { it }
                    .map { ToolStatatistics(it.key, it.value.size) }

    private data class ToolDescriptor(val id: String, val description: String, val isAdditionalCase: Boolean = false)

    private data class ToolStatatistics(val descriptor: ToolDescriptor, val count: Int)

    companion object {
        const val GROUP_NAME = ".NET"

        private val DOTNET_GROUP: PositionAware = object : PositionAware {
            override fun getOrderId()= DotnetConstants.RUNNER_TYPE
            override fun getConstraint() = PositionConstraint.UNDEFINED
        }

        private const val MSBUILD_RUNNER = "MSBuild"
        private const val VS_RUNNER = "Visual Studio (sln)"
        private const val VSTESTS_RUNNER = "Visual Studio Tests VSTest"
        private const val MSTESTS_RUNNER = "Visual Studio Tests MSTest"

        private val DefaultMSBuildVersion = Tool.MSBuildCrossPlatform.id
        private val DefaultVSVersion = Tool.VisualStudioAny.id
        private val DefaultVSTestVersion = Tool.VSTestCrossPlatform.id
        private val NugetInstallerToolDesc = sequenceOf(ToolDescriptor("nuget.installer", "NuGet: Installer"))
        private val NugetPublishToolDesc = sequenceOf(ToolDescriptor("nuget.publish", "NuGet: Publish"))
        private val NugetPackToolDesc = sequenceOf(ToolDescriptor("nuget.pack", "NuGet: Pack"))
    }
}


package jetbrains.buildServer.dotnet

import jetbrains.buildServer.ArtifactsConstants
import jetbrains.buildServer.coverage.agent.serviceMessage.CoverageServiceMessageSetup
import jetbrains.buildServer.dotNet.DotNetConstants

/**
 * Coverage constants.
 */
object CoverageConstants {
    const val PARAM_TYPE = "dotNetCoverage.tool"
    const val COVERAGE_TYPE = "dotNetCoverageDotnetRunner"
    const val COVERAGE_REPORT_HOME = ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + "/.NETCoverage"
    const val COVERAGE_REPORT_MULTIPLE = "$COVERAGE_REPORT_HOME/results"
    // Those constants are required to have Coverage tab added and THE PART OF CONTRACT
    const val COVERAGE_HTML_REPORT_ZIP = "coverage.zip"
    const val COVERAGE_PUBLISH_PATH_PARAM = "teamcity.agent.dotNetCoverage.publishPath"
    // Those constants are required to publish well-known artifacts and THE PART OF CONTRACT
    const val COVERAGE_REPORT_NAME = "CoverageReport"
    const val COVERAGE_REPORT_EXT = ".xml"
    // Specifies custom coverage report .html file name
    const val COVERAGE_HTML_REPORT_INDEX_KEY = "dotNetCoverage.index"

    // dotCover
    const val DOTCOVER_PACKAGE_ID = "JetBrains.dotCover.CommandLineTools"           // originally Windows-only and since 2023.3.* – x-platform without .NET runtime
    const val DOTCOVER_DEPRECATED_PACKAGE_ID = "JetBrains.dotCover.DotNetCliTool"   // deprecated cross-platform package with bundled .NET runtime
    const val DOT_COVER_TOOL_TYPE_NAME = "JetBrains dotCover Command Line Tools"
    const val DOT_COVER_SHORT_TOOL_TYPE_NAME = "dotCover CLT"
    const val DOT_COVER_TARGET_FILE_DISPLAY_NAME = "dotCover CLT home directory"
    const val BUNDLED_TOOL_VERSION_NAME = "bundled"
    const val DOTCOVER_BUNDLED_NUSPEC_FILE_PATH = "server/bundled-tools/JetBrains.dotCover.CommandLineTool/$DOTCOVER_PACKAGE_ID.nuspec"
    const val DOTCOVER_BUNDLED_AGENT_TOOL_PACKAGE_PATH = "server/bundled-tools/JetBrains.dotCover.CommandLineTool/$DOTCOVER_PACKAGE_ID.bundled.zip"
    
    // dotCover tool postfixes
    const val DOTCOVER_POSTFIX = ""
    const val DOTCOVER_CROSS_PLATFORM_POSTFIX = "Cross-Platform"
    const val DOTCOVER_CROSS_PLATFORM_DEPRECATED_POSTFIX = "Cross-Platform (deprecated)" // deprecated cross-platform version with bundled .NET runtime
    const val DOTCOVER_WINDOWS_POSTFIX = "Windows"

    const val DOTCOVER_RUNNER_TYPE = "dotcover"
    const val PARAM_DOTCOVER = "dotcover"
    const val PARAM_DOTCOVER_HOME = "dotNetCoverage.dotCover.home.path"
    const val PARAM_DOTCOVER_FILTERS = "dotNetCoverage.dotCover.filters"
    const val PARAM_DOTCOVER_ATTRIBUTE_FILTERS = "dotNetCoverage.dotCover.attributeFilters"
    const val PARAM_DOTCOVER_ARGUMENTS = "dotNetCoverage.dotCover.customCmd"
    const val PARAM_DOTCOVER_MODE = "dotNetCoverage.dotCover.mode"
    const val PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE = "dotNetCoverage.dotCover.coveredProcessExecutable"
    const val PARAM_DOTCOVER_COVERED_PROCESS_ARGUMENTS = "dotNetCoverage.dotCover.coveredProcessArguments"
    const val PARAM_DOTCOVER_GENERATE_REPORT = "dotNetCoverage.dotCover.generateReport"
    const val PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS = "dotNetCoverage.dotCover.additionalShapshotPaths"
    const val PARAM_DOTCOVER_LOG_PATH = "teamcity.agent.dotCover.log"

    const val TEAMCITY_DOTCOVER_HOME = "teamcity.dotCover.home"
    const val DOTCOVER_ARTIFACTS_DIR = "artifacts"
    const val DOTCOVER_PUBLISH_SNAPSHOT_PARAM = "teamcity.agent.dotCover.publishSnapshot"
    const val DOTCOVER_SNAPSHOT_FILE_EXTENSION = ".dcvr"
    const val DOTCOVER_SNAPSHOT_DCVR = "dotCover$DOTCOVER_SNAPSHOT_FILE_EXTENSION"
    const val DOTCOVER_LOGS = "dotCoverLogs.zip"

    const val DOTCOVER_TOOL_NAME = "dotCover"
    const val DOTCOVER_BUNDLED_TOOL_ID = "${DOTCOVER_PACKAGE_ID}.${BUNDLED_TOOL_VERSION_NAME}"

    val DOTNET_FRAMEWORK_PATTERN_3_5 = DotNetConstants.DOTNET_FRAMEWORK_3_5.replace(".", "\\.") + "_.+|" + DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.[\\d\\.]+_.+"
    const val DOTNET_FRAMEWORK_4_6_1_PATTERN = DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.(6\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_.+"
    const val DOTNET_FRAMEWORK_4_7_2_PATTERN = DotNetConstants.DOTNET_FRAMEWORK_4 + "\\.(7\\.(?!0)|[7-9]|[\\d]{2,})[\\d\\.]*_.+"


    class ServiceMessageSetup(setup: CoverageServiceMessageSetup) {
        init {
            setup.addPropertyMapping(
                "dotcover_home",
                PARAM_DOTCOVER_HOME
            )
        }
    }
}
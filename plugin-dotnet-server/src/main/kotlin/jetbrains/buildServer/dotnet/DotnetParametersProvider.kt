/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.commands.*
import jetbrains.buildServer.dotnet.discovery.SdkResolverImpl
import jetbrains.buildServer.web.functions.InternalProperties

/**
 * Provides parameters for dotnet runner.
 */
class DotnetParametersProvider {

    val commands: Collection<CommandType> = commandTypes.values
    val coverages: Collection<CommandType> = coverageTypes.values

    val experimentalMode get() = DotnetParametersProvider.experimentalMode

    // Command parameters

    val argumentsKey: String
        get() = DotnetConstants.PARAM_ARGUMENTS

    val commandKey: String
        get() = DotnetConstants.PARAM_COMMAND

    val configKey: String
        get() = DotnetConstants.PARAM_CONFIG

    val frameworkKey: String
        get() = DotnetConstants.PARAM_FRAMEWORK

    val requiredSdkKey: String
        get() = DotnetConstants.PARAM_REQUIRED_SDK

    val msbuildVersionKey: String
        get() = DotnetConstants.PARAM_MSBUILD_VERSION

    val msbuildVersions: List<Tool>
        get() = Tool.values().filter {
            it.type == ToolType.MSBuild
            && (experimentalMode || it.platform != ToolPlatform.Mono)
            && (supportMSBuildBitness || it.bitness == ToolBitness.Any)
        }

    val nugetApiKey: String
        get() = DotnetConstants.PARAM_NUGET_API_KEY

    val nugetPackageIdKey: String
        get() = DotnetConstants.PARAM_NUGET_PACKAGE_ID

    val nugetPackageSourceKey: String
        get() = DotnetConstants.PARAM_NUGET_PACKAGE_SOURCE

    val nugetPackageSourcesKey: String
        get() = DotnetConstants.PARAM_NUGET_PACKAGE_SOURCES

    val nugetPackagesDirKey: String
        get() = DotnetConstants.PARAM_NUGET_PACKAGES_DIR

    val nugetConfigFileKey: String
        get() = DotnetConstants.PARAM_NUGET_CONFIG_FILE

    val nugetNoSymbolsKey: String
        get() = DotnetConstants.PARAM_NUGET_NO_SYMBOLS

    val skipBuildKey: String
        get() = DotnetConstants.PARAM_SKIP_BUILD

    val outputDirKey: String
        get() = DotnetConstants.PARAM_OUTPUT_DIR

    val pathsKey: String
        get() = DotnetConstants.PARAM_PATHS

    val platformKey: String
        get() = DotnetConstants.PARAM_PLATFORM

    val runtimeKey: String
        get() = DotnetConstants.PARAM_RUNTIME

    val targetsKey: String
        get() = DotnetConstants.PARAM_TARGETS

    val testFilterKey: String
        get() = DotnetConstants.PARAM_TEST_FILTER

    val testNamesKey: String
        get() = DotnetConstants.PARAM_TEST_NAMES

    val testCaseFilterKey: String
        get() = DotnetConstants.PARAM_TEST_CASE_FILTER

    val testSettingsFileKey: String
        get() = DotnetConstants.PARAM_TEST_SETTINGS_FILE

    val visualStudioActionKey: String
        get() = DotnetConstants.PARAM_VISUAL_STUDIO_ACTION

    val visualStudioActions: List<VisualStudioAction>
        get() = VisualStudioAction.values().asList()

    val visualStudioVersionKey: String
        get() = DotnetConstants.PARAM_VISUAL_STUDIO_VERSION

    val visualStudioVersions: List<Tool>
        get() = Tool.values().filter { it.type == ToolType.VisualStudio }

    val verbosityKey: String
        get() = DotnetConstants.PARAM_VERBOSITY

    val verbosityValues: List<Verbosity>
        get() = Verbosity.values().toList()

    val versionSuffixKey: String
        get() = DotnetConstants.PARAM_VERSION_SUFFIX

    val vstestVersionKey: String
        get() = DotnetConstants.PARAM_VSTEST_VERSION

    val vstestVersions: List<Tool>
        get() = Tool.values().filter { it.type == ToolType.VSTest }

    val vstestPlatforms: List<Platform>
        get() = Platform.values().toList()

    val vstestInIsolation: String
        get() = DotnetConstants.PARAM_VSTEST_IN_ISOLATION

    val singleSessionKey: String
        get() = DotnetConstants.PARAM_SINGLE_SESSION

    // Coverage keys

    val coverageTypeKey: String
        get() = CoverageConstants.PARAM_TYPE

    val dotCoverHomeKey: String
        get() = CoverageConstants.PARAM_DOTCOVER_HOME

    val dotCoverFiltersKey: String
        get() = CoverageConstants.PARAM_DOTCOVER_FILTERS

    val dotCoverAttributeFiltersKey: String
        get() = CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS

    val dotCoverArgumentsKey: String
        get() = CoverageConstants.PARAM_DOTCOVER_ARGUMENTS

    companion object {
        private val experimentalMode get() = InternalProperties.getBoolean(DotnetConstants.PARAM_EXPERIMENTAL) ?: false
        private val supportMSBuildBitness get() = InternalProperties.getBoolean(DotnetConstants.PARAM_SUPPORT_MSBUILD_BITNESS) ?: false
        private val experimentalCommandTypes: Sequence<CommandType> = sequenceOf()
        private val requirementFactory: RequirementFactory = RequirementFactoryImpl(SdkResolverImpl(SdkTypeResolverImpl()))
        val commandTypes
            get() = sequenceOf(
                    RestoreCommandType(requirementFactory),
                    BuildCommandType(requirementFactory),
                    TestCommandType(requirementFactory),
                    PublishCommandType(requirementFactory),
                    PackCommandType(requirementFactory),
                    NugetPushCommandType(requirementFactory),
                    NugetDeleteCommandType(requirementFactory),
                    CleanCommandType(requirementFactory),
                    RunCommandType(requirementFactory),
                    MSBuildCommandType(requirementFactory),
                    VSTestCommandType(requirementFactory),
                    VisualStudioCommandType(requirementFactory))
                    .plus(if(experimentalMode) experimentalCommandTypes else emptySequence())
                    .sortedBy { it.description }
                    .plus(CustomCommandType(requirementFactory))
                    .associateBy { it.name }

        val coverageTypes
            get() = sequenceOf<CommandType>(DotCoverCoverageType(requirementFactory))
                    .sortedBy { it.name }
                    .associateBy { it.name }
    }
}

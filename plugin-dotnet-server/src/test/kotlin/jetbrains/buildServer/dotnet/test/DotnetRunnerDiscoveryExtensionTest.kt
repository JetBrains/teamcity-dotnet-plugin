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

package jetbrains.buildServer.dotnet.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetRunnerDiscoveryExtensionTest {
    @DataProvider
    fun testGenerateCommandsData(): Array<Array<Any>> {
        val restore1 = createCommand(DotnetCommandType.Restore, "dir/mypro.proj")
        val build1 = createCommand(DotnetCommandType.Build, "dir/mypro.proj")
        val msbuild1 = createMSBuildCommand("dir/mypro.proj")
        val test1 = createCommand(DotnetCommandType.Test, "dir/mypro.proj")
        val vstest1 = createVSTestCommand("dir/bin/**/abc.dll")
        val restore2 = createCommand(DotnetCommandType.Restore, "dir2/My.sln")
        val build2 = createCommand(DotnetCommandType.Build, "dir2/My.sln")
        val publish1 = createCommand(DotnetCommandType.Publish, "dir/mypro.proj")
        val defaultProjectTypeMap = mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Unknown))
        return arrayOf(
                // Default project command is build
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), false)))),
                        defaultProjectTypeMap,
                        listOf(restore1, build1)),
                // Native msbuild
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), false, emptyList())))),
                        defaultProjectTypeMap,
                        listOf(restore1, msbuild1, build1)),
                // Only native msbuild
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), listOf(Framework("net35")), emptyList(), emptyList(), emptyList(), false, emptyList())))),
                        defaultProjectTypeMap,
                        listOf(createMSBuildCommand("dir/mypro.proj", "3.5 4.8"))),
                // Does not genere Restore when >= netcoreapp2*
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), listOf(Framework("netcoreapp1.0"), Framework("netcoreapp2.1")), emptyList(), emptyList())))),
                        defaultProjectTypeMap,
                        listOf(createCommand(DotnetCommandType.Build, "dir/mypro.proj", "1.0 2.0 2.1"))),
                // Does not genere Restore when >= netcoreapp2* for solution
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), listOf(Framework("netcoreapp1.0"), Framework("netcoreapp3.0")), emptyList(), emptyList())), "abc.sln")),
                        defaultProjectTypeMap,
                        listOf(createCommand(DotnetCommandType.Build, "abc.sln", "1.0 2.0 3.0"))),
                // Requires SDK
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), listOf(Framework("netstandard2.1")), emptyList(), emptyList())))),
                        defaultProjectTypeMap,
                        listOf(createCommand(DotnetCommandType.Build, "dir/mypro.proj", "1.0 3.5"))),
                // Path is case sensitive
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/myproj.proj", emptyList(), emptyList(), emptyList(), emptyList()), Project("dir/MyProj.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        mapOf<String, Set<ProjectType>>("dir/myproj.proj" to setOf<ProjectType>(ProjectType.Unknown), "dir/MyProj.proj" to setOf<ProjectType>(ProjectType.Unknown)),
                        listOf(createCommand(DotnetCommandType.Restore, "dir/myproj.proj"), createCommand(DotnetCommandType.Build, "dir/myproj.proj"), createCommand(DotnetCommandType.Restore, "dir/MyProj.proj"), createCommand(DotnetCommandType.Build, "dir/MyProj.proj"))),
                // Default project command is build with some refs
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("abc")))))),
                        defaultProjectTypeMap,
                        listOf(restore1, build1)),
                // Normalize project name
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir\\mypro.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        mapOf<String, Set<ProjectType>>("dir\\mypro.proj" to setOf<ProjectType>(ProjectType.Unknown)),
                        listOf(restore1, build1)),
                // Test project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(restore1, test1)),
                // Native test project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")), emptyList(), false, listOf(Property("AssemblyName", "abc")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(restore1, msbuild1, test1, vstest1)),
                // Only native test project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), listOf(Framework("net20")), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")), emptyList(), false, listOf(Property("AssemblyName", "abc")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(msbuild1, vstest1)),
                // Distinct similar tests
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"), Reference("Abc"))), Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(restore1, test1)),
                // Test project has priority vs publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"), Reference("Microsoft.NET.Test.Sdk")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test, ProjectType.Publish)),
                        listOf(restore1, test1)),
                // Solution for tests is working when all project are tests
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"), Reference("Microsoft.NET.Test.Sdk")))), "dir2\\My.sln")),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(restore2, build2, createCommand(DotnetCommandType.Test, "dir2/My.sln"), createCommand(DotnetCommandType.Test, "dir/mypro.proj"))),
                // Solution for tests is no working when at least one project is no tests
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"), Reference("Microsoft.NET.Test.Sdk")))), "dir2\\My.sln")),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test, ProjectType.Publish)),
                        listOf(restore2, build2, createCommand(DotnetCommandType.Test, "dir2/My.sln"), createCommand(DotnetCommandType.Test, "dir/mypro.proj"))),
                // Solution has has priority vs project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("abc")))), "dir3/my.sln")),
                        defaultProjectTypeMap,
                        listOf(createCommand(DotnetCommandType.Restore, "dir3/my.sln"), createCommand(DotnetCommandType.Build, "dir3/my.sln"))),
                // Publish project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Publish)),
                        listOf(restore1, publish1)),
                // Distinct similar publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"))), Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("ggg"), Reference("Microsoft.aspnet.DDD")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Publish)),
                        listOf(restore1, publish1)),
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.Aspnetaaa")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Publish)),
                        listOf(restore1, publish1)),
                // Empty project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("", emptyList(), emptyList(), emptyList(), emptyList())))),
                        defaultProjectTypeMap,
                        emptyList<DiscoveredTarget>()),
                // Empty solution
                arrayOf(
                        sequenceOf(Solution(emptyList())),
                        defaultProjectTypeMap,
                        emptyList<DiscoveredTarget>()),
                // Empty solutions
                arrayOf(
                        emptySequence<Solution>(),
                        defaultProjectTypeMap,
                        emptyList<DiscoveredTarget>()),
                // Skip test project from proj when it uses in some solution
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))), "dir2\\my.sln")),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(createCommand(DotnetCommandType.Restore, "dir2/my.sln"), createCommand(DotnetCommandType.Build, "dir2/my.sln"), createCommand(DotnetCommandType.Test, "dir2/my.sln"), createCommand(DotnetCommandType.Test, "dir/mypro.proj"))))
    }

    @Test(dataProvider = "testGenerateCommandsData")
    fun shouldDiscoveryAndGenerateCommands(
            solutions: Sequence<Solution>,
            projectTypes: Map<String, Set<ProjectType>>,
            expectedTargets: List<DiscoveredTarget>) {
        // Given
        val paths = sequenceOf("dir1/proj1.csproj", "dir2/proj2.json")
        val solutionDiscover = mockk<SolutionDiscover>()
        val streamFactory = mockk<StreamFactory>()
        val projectTypeSelector = mockk<ProjectTypeSelector>()
        val sdkWizard = mockk<SdkWizard>()

        for (solution in solutions) {
            for (project in solution.projects) {
                val types = projectTypes.get(project.project)
                if (types != null) {
                    every { projectTypeSelector.select(project) } returns types
                } else {
                    every { projectTypeSelector.select(project) } returns emptySet<ProjectType>()
                }
            }
        }

        every { solutionDiscover.discover(streamFactory, paths) } returns solutions.toList()
        every { solutionDiscover.discover(streamFactory, paths) } returns solutions.toList()
        every { sdkWizard.suggestSdks(any(), true) } answers {
            val frameworks = arg<Sequence<Project>>(0).flatMap { it.frameworks.asSequence() }.map { it.name }.toHashSet()
            sequence {
                if (frameworks.contains("net35")) {
                    yield(SdkVersion(Version(3, 5), SdkType.FullDotnetTargetingPack, SdkVersionType.Default))
                    yield(SdkVersion(Version(4, 8), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible))
                }
                if (frameworks.contains("netcoreapp1.0")) {
                    yield(SdkVersion(Version(1, 0), SdkType.DotnetCore, SdkVersionType.Default))
                    yield(SdkVersion(Version(2, 0), SdkType.DotnetCore, SdkVersionType.Compatible))
                }
                if (frameworks.contains("netcoreapp2.1")) yield(SdkVersion(Version(2, 1), SdkType.DotnetCore, SdkVersionType.Default))
                if (frameworks.contains("netcoreapp3.0")) yield(SdkVersion(Version(3, 0), SdkType.DotnetCore, SdkVersionType.Default))
                if (frameworks.contains("netstandard2.1")) yieldAll(sequenceOf(SdkVersion(Version(1, 0), SdkType.Dotnet, SdkVersionType.Default), SdkVersion(Version(3, 5), SdkType.FullDotnetTargetingPack, SdkVersionType.Compatible)))
            }
        }

        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector, sdkWizard)

        // When
        val actualTargets = discoveryExtension.discover(streamFactory, paths).toList()

        // Then
        Assert.assertEquals(actualTargets, expectedTargets)
    }

    @DataProvider
    fun testGetNewCommandsData(): Array<Array<Sequence<DotnetRunnerDiscoveryExtension.Command>>> {
        val command1 = DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))
        val command2 = DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "Value1")))
        return arrayOf(
                // New command1
                arrayOf(
                        sequenceOf(command1),
                        sequenceOf(command1, DotnetRunnerDiscoveryExtension.Command("c", listOf(DotnetRunnerDiscoveryExtension.Parameter("param2", "value2")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("c", listOf(DotnetRunnerDiscoveryExtension.Parameter("param2", "value2"))))),
                arrayOf(
                        sequenceOf(),
                        sequenceOf(command2),
                        sequenceOf(command2)),
                // Commands case insensitive
                arrayOf(
                        sequenceOf(command1),
                        sequenceOf(command2),
                        sequenceOf(command2)),
                arrayOf(
                        sequenceOf(command1),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("Param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("Param1", "value1"))))),
                // Has all commands
                arrayOf(
                        sequenceOf(),
                        sequenceOf(),
                        sequenceOf()),
                arrayOf(
                        sequenceOf(command1),
                        sequenceOf(command1),
                        sequenceOf()),
                arrayOf(
                        sequenceOf(command1),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("A", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf()),
                // Has all commands with dif names
                arrayOf(
                        sequenceOf(command1),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("b", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf()))
    }

    @Test(dataProvider = "testGetNewCommandsData")
    fun shouldGetNewCommands(
            existingCommands: Sequence<DotnetRunnerDiscoveryExtension.Command>,
            createdCommands: Sequence<DotnetRunnerDiscoveryExtension.Command>,
            expectedCommands: Sequence<DotnetRunnerDiscoveryExtension.Command>) {
        // Given
        val solutionDiscover = mockk<SolutionDiscover>()
        val projectTypeSelector = mockk<ProjectTypeSelector>()
        val sdkWizard = mockk<SdkWizard>()
        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector, sdkWizard)

        // When
        val actualCommands = discoveryExtension.getNewCommands(existingCommands, createdCommands).toList()

        // Then
        Assert.assertEquals(actualCommands, expectedCommands.toList())
    }

    @Test
    fun shouldGetCreatedCommands() {
        // Given
        val solutionDiscover = mockk<SolutionDiscover>()
        val projectTypeSelector = mockk<ProjectTypeSelector>()
        val sdkWizard = mockk<SdkWizard>()
        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector, sdkWizard)
        val discoveredTarget1 = DiscoveredTarget(DotnetConstants.RUNNER_TYPE, "abc", mapOf(DotnetConstants.PARAM_COMMAND to "value1"))
        val discoveredTarget2 = DiscoveredTarget(DotnetConstants.RUNNER_TYPE, "xyz", mapOf(DotnetConstants.PARAM_PATHS to "value2"))

        // When
        val actualCommands = discoveryExtension.getCreatedCommands(listOf<DiscoveredObject>(discoveredTarget1, discoveredTarget2).toMutableList()).toList()

        // Then
        Assert.assertEquals(actualCommands, listOf(DotnetRunnerDiscoveryExtension.Command("abc", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_COMMAND, "value1"))), DotnetRunnerDiscoveryExtension.Command("xyz", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_PATHS, "value2")))))
    }

    @Test
    fun shouldGetExistingCommands() {
        // Given
        val solutionDiscover = mockk<SolutionDiscover>()
        val buildRunnerDescriptor1 = mockk<SBuildRunnerDescriptor>()
        val buildRunnerDescriptor2 = mockk<SBuildRunnerDescriptor>()
        val buildRunnerDescriptor3 = mockk<SBuildRunnerDescriptor>()
        val buildTypeSettings = mockk<BuildTypeSettings>()
        val projectTypeSelector = mockk<ProjectTypeSelector>()
        val sdkWizard = mockk<SdkWizard>()
        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector, sdkWizard)

        every { buildTypeSettings.buildRunners } returns listOf(buildRunnerDescriptor1, buildRunnerDescriptor2, buildRunnerDescriptor3)
        every { buildRunnerDescriptor1.runType } returns MyRunType(DotnetConstants.RUNNER_TYPE)
        every { buildRunnerDescriptor1.name } returns "abc"
        every { buildRunnerDescriptor1.parameters } returns mapOf(DotnetConstants.PARAM_COMMAND to "value1")
        every { buildRunnerDescriptor2.runType } returns MyRunType("jjj")
        every { buildRunnerDescriptor2.name } returns "abc"
        every { buildRunnerDescriptor2.parameters } returns mapOf(DotnetConstants.PARAM_COMMAND to "value1")
        every { buildRunnerDescriptor3.runType } returns MyRunType(DotnetConstants.RUNNER_TYPE)
        every { buildRunnerDescriptor3.name } returns "xyz"
        every { buildRunnerDescriptor3.parameters } returns mapOf(DotnetConstants.PARAM_PATHS to "value2")

        // When
        val actualCommands = discoveryExtension.getExistingCommands(buildTypeSettings).toList()

        // Then
        Assert.assertEquals(actualCommands, listOf(DotnetRunnerDiscoveryExtension.Command("abc", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_COMMAND, "value1"))), DotnetRunnerDiscoveryExtension.Command("xyz", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_PATHS, "value2")))))
    }

    private fun createCommand(commandType: DotnetCommandType, path: String, requiredSdk: String = ""): DiscoveredTarget {
        val target = DiscoveredTarget(
                DotnetConstants.RUNNER_TYPE,
                "name",
                mapOf(
                        DotnetConstants.PARAM_COMMAND to commandType.id,
                        DotnetConstants.PARAM_PATHS to path
                ))

        if (requiredSdk.isNotEmpty()) {
            target.parameters[DotnetConstants.PARAM_REQUIRED_SDK] = requiredSdk
        }

        return target;
    }
    private fun createMSBuildCommand(path: String, requiredSdk: String = ""): DiscoveredTarget {
        val target =  DiscoveredTarget(
                DotnetConstants.RUNNER_TYPE,
                "name",
                mapOf(
                        DotnetConstants.PARAM_COMMAND to DotnetCommandType.MSBuild.id,
                        DotnetConstants.PARAM_PATHS to path, DotnetConstants.PARAM_ARGUMENTS to "-restore -noLogo",
                        DotnetConstants.PARAM_MSBUILD_VERSION to Tool.values().filter { it.type == ToolType.MSBuild && it.bitness == ToolBitness.Any }.sortedBy { it.version }.reversed().first().id
                ))

        if (requiredSdk.isNotEmpty()) {
            target.parameters[DotnetConstants.PARAM_REQUIRED_SDK] = requiredSdk
        }

        return target
    }

    private fun createVSTestCommand(path: String) = DiscoveredTarget(
            DotnetConstants.RUNNER_TYPE,
            "name",
            mapOf(
                    DotnetConstants.PARAM_COMMAND to DotnetCommandType.VSTest.id,
                    DotnetConstants.PARAM_PATHS to path,
                    DotnetConstants.PARAM_VSTEST_VERSION to Tool.values().filter { it.type == ToolType.VSTest }.sortedBy { it.version }.reversed().first().id
            ))

    private class MyRunType(private val type: String) : RunType() {
        override fun getViewRunnerParamsJspFilePath(): String? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getRunnerPropertiesProcessor(): PropertiesProcessor? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getDescription(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getEditRunnerParamsJspFilePath(): String? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getType(): String = type

        override fun getDisplayName(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getDefaultRunnerProperties(): MutableMap<String, String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}
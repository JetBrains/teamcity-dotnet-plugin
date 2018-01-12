package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetRunnerDiscoveryExtensionTest {
    @DataProvider
    fun testGenerateCommandsData(): Array<Array<Any>> {
        return arrayOf(
                // Default project command is build
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Distinct similar default
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList()), Project("dir\\mypro.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Path is case sensitive
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/myproj.proj", emptyList(), emptyList(), emptyList(), emptyList()), Project("dir/MyProj.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/myproj.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/myproj.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/MyProj.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/MyProj.proj")))),
                // Default project command is build with some refs
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("abc")))))),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Normalize project name
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir\\mypro.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Test project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Distinct similar tests
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"), Reference("Abc"))), Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Test project has priority vs publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"), Reference("Microsoft.NET.Test.Sdk")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test, ProjectType.Publish)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Solution has has priority vs test, publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"), Reference("Microsoft.NET.Test.Sdk")))), "dir2\\My.sln")),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test, ProjectType.Publish)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")))),
                // Solution has has priority vs project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("abc")))), "dir3/my.sln")),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir3/my.sln")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir3/my.sln")))),
                // Publish project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Publish)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Distinct similar publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"))), Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("ggg"), Reference("Microsoft.aspnet.DDD")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Publish)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.Aspnetaaa")))))),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Publish)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Empty project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("", emptyList(), emptyList(), emptyList(), emptyList())))),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        emptyList<DiscoveredTarget>()),
                // Empty solution
                arrayOf(
                        sequenceOf(Solution(emptyList())),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        emptyList<DiscoveredTarget>()),
                // Empty solutions
                arrayOf(
                        emptySequence<Solution>(),
                        mapOf<String, Set<ProjectType>>("" to setOf<ProjectType>()),
                        emptyList<DiscoveredTarget>()),
                // Skip test project from proj when it uses in some solution
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))), "dir2\\my.sln")),
                        mapOf<String, Set<ProjectType>>("dir/mypro.proj" to setOf<ProjectType>(ProjectType.Test)),
                        listOf(DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir2/my.sln")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir2/my.sln")), DiscoveredTarget("name", mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir2/my.sln")))))
    }

    @Test(dataProvider = "testGenerateCommandsData")
    fun shouldDiscoveryAndGenerateCommands(
            solutions: Sequence<Solution>,
            projectTypes: Map<String, Set<ProjectType>>,
            expectedTargets: List<DiscoveredTarget>) {
        // Given
        val paths = sequenceOf("dir1/proj1.csproj", "dir2/proj2.json")
        val ctx = Mockery()
        val solutionDiscover = ctx.mock(SolutionDiscover::class.java)
        val streamFactory = ctx.mock(StreamFactory::class.java)
        val projectTypeSelector = ctx.mock(ProjectTypeSelector::class.java)

        ctx.checking(object : Expectations() {
            init {
                for (solution in solutions) {
                    for (project in solution.projects) {
                        val types = projectTypes.get(project.project)
                        if (types != null) {
                            allowing<ProjectTypeSelector>(projectTypeSelector).select(project)
                            will(returnValue(types))
                        }
                        else {
                            allowing<ProjectTypeSelector>(projectTypeSelector).select(project)
                            will(returnValue(emptySet<ProjectType>()))
                        }
                    }
                }
                
                oneOf<SolutionDiscover>(solutionDiscover).discover(streamFactory, paths)
                will(returnValue(solutions))

                allowing<SolutionDiscover>(solutionDiscover).discover(streamFactory, paths)
                will(returnValue(solutions))
            }
        })

        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector)

        // When
        val actualTargets = discoveryExtension.discover(streamFactory, paths).toList()

        // Then
        Assert.assertEquals(actualTargets, expectedTargets)
    }

    @DataProvider
    fun testGetNewCommandsData(): Array<Array<Sequence<DotnetRunnerDiscoveryExtension.Command>>> {
        return arrayOf(
                // New command
                arrayOf(
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1"))), DotnetRunnerDiscoveryExtension.Command("c", listOf(DotnetRunnerDiscoveryExtension.Parameter("param2", "value2")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("c", listOf(DotnetRunnerDiscoveryExtension.Parameter("param2", "value2"))))),
                arrayOf(
                        sequenceOf(),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "Value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "Value1"))))),
                // Commands case insensitive
                arrayOf(
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "Value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "Value1"))))),
                arrayOf(
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("Param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("Param1", "value1"))))),
                // Has all commands
                arrayOf(
                        sequenceOf(),
                        sequenceOf(),
                        sequenceOf()),
                arrayOf(
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf()),
                arrayOf(
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("A", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf()),
                // Has all commands with dif names
                arrayOf(
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("a", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf(DotnetRunnerDiscoveryExtension.Command("b", listOf(DotnetRunnerDiscoveryExtension.Parameter("param1", "value1")))),
                        sequenceOf()))}

    @Test(dataProvider = "testGetNewCommandsData")
    fun shouldGetNewCommands(
            existingCommands: Sequence<DotnetRunnerDiscoveryExtension.Command>,
            createdCommands: Sequence<DotnetRunnerDiscoveryExtension.Command>,
            expectedCommands: Sequence<DotnetRunnerDiscoveryExtension.Command>) {
        // Given
        val ctx = Mockery()
        val solutionDiscover = ctx.mock(SolutionDiscover::class.java)
        val projectTypeSelector = ctx.mock(ProjectTypeSelector::class.java)
        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector)

        // When
        val actualCommands = discoveryExtension.getNewCommands(existingCommands, createdCommands).toList()

        // Then
        Assert.assertEquals(actualCommands, expectedCommands.toList())
    }

    @Test
    fun shouldGetCreatedCommands() {
        // Given
        val ctx = Mockery()
        val solutionDiscover = ctx.mock(SolutionDiscover::class.java)
        val projectTypeSelector = ctx.mock(ProjectTypeSelector::class.java)
        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector)
        val discoveredTarget1 = DiscoveredTarget("abc", mapOf(DotnetConstants.PARAM_COMMAND to "value1"))
        val discoveredTarget2 = DiscoveredTarget("xyz", mapOf(DotnetConstants.PARAM_PATHS to "value2"))

        // When
        val actualCommands = discoveryExtension.getCreatedCommands(listOf<DiscoveredObject>(discoveredTarget1, discoveredTarget2).toMutableList()).toList()

        // Then
        Assert.assertEquals(actualCommands, listOf(DotnetRunnerDiscoveryExtension.Command("abc", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_COMMAND, "value1"))), DotnetRunnerDiscoveryExtension.Command("xyz", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_PATHS, "value2")))))
    }

    @Test
    fun shouldGetExistingCommands() {
        // Given
        val ctx = Mockery()
        val solutionDiscover = ctx.mock(SolutionDiscover::class.java)
        val buildRunnerDescriptor1 = ctx.mock(SBuildRunnerDescriptor::class.java, "buildRunnerDescriptor1")
        val buildRunnerDescriptor2 = ctx.mock(SBuildRunnerDescriptor::class.java, "buildRunnerDescriptor2")
        val buildRunnerDescriptor3 = ctx.mock(SBuildRunnerDescriptor::class.java, "buildRunnerDescriptor3")
        val buildTypeSettings = ctx.mock(BuildTypeSettings::class.java)
        val projectTypeSelector = ctx.mock(ProjectTypeSelector::class.java)
        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover, DiscoveredTargetNameFactoryStub("name"), projectTypeSelector)
        ctx.checking(object : Expectations() {
            init {
                oneOf<BuildTypeSettings>(buildTypeSettings).buildRunners
                will(returnValue(listOf(buildRunnerDescriptor1, buildRunnerDescriptor2, buildRunnerDescriptor3)))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor1).runType
                will(returnValue(MyRunType(DotnetConstants.RUNNER_TYPE)))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor1).name
                will(returnValue("abc"))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor1).parameters
                will(returnValue(mapOf(DotnetConstants.PARAM_COMMAND to "value1")))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor2).runType
                will(returnValue(MyRunType("jjj")))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor2).name
                will(returnValue("abc"))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor2).parameters
                will(returnValue(mapOf(DotnetConstants.PARAM_COMMAND to "value1")))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor3).runType
                will(returnValue(MyRunType(DotnetConstants.RUNNER_TYPE)))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor3).name
                will(returnValue("xyz"))

                oneOf<SBuildRunnerDescriptor>(buildRunnerDescriptor3).parameters
                will(returnValue(mapOf(DotnetConstants.PARAM_PATHS to "value2")))
            }
        })


        // When
        val actualCommands = discoveryExtension.getExistingCommands(buildTypeSettings).toList()

        // Then
        Assert.assertEquals(actualCommands, listOf(DotnetRunnerDiscoveryExtension.Command("abc", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_COMMAND, "value1"))), DotnetRunnerDiscoveryExtension.Command("xyz", listOf(DotnetRunnerDiscoveryExtension.Parameter(DotnetConstants.PARAM_PATHS, "value2")))))
    }

    private class MyRunType(private val type: String): RunType() {
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
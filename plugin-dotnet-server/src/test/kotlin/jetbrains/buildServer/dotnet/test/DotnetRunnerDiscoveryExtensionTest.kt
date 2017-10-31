package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.discovery.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetRunnerDiscoveryExtensionTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                // Default project command is build
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Distinct similar default
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList()), Project("dir\\mypro.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Path is case sensitive
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/myproj.proj", emptyList(), emptyList(), emptyList(), emptyList()), Project("dir/MyProj.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/myproj.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/myproj.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/MyProj.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/MyProj.proj")))),
                // Default project command is build with some refs
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("abc")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Normalize project name
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir\\mypro.proj", emptyList(), emptyList(), emptyList(), emptyList())))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Test project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Test project case insensitive
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.TEST.sdk")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Test project with mult refs
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Abc"), Reference( "Microsoft.NET.Test.SDK")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Distinct similar tests
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"), Reference("Abc"))), Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Test project has priority vs publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"), Reference("Microsoft.NET.Test.Sdk")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Solution has has priority vs test, publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"), Reference("Microsoft.NET.Test.Sdk")))), "dir2\\My.sln")),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir2/My.sln")))),
                // Solution has has priority vs project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("abc")))), "dir3/my.sln")),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir3/my.sln")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir3/my.sln")))),
                // Publish project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Publish project when generatePackageOnBuild is true
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), true)))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Publish project case insensitive
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("MicrosofT.ASPnet")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Publish project with other ids
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet.abc")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Distinct similar publish
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.aspnet"))), Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("ggg"), Reference("Microsoft.aspnet.DDD")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.Aspnetaaa")))))),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Publish.id, DotnetConstants.PARAM_PATHS to "dir/mypro.proj")))),
                // Empty project
                arrayOf(
                        sequenceOf(Solution(listOf(Project("", emptyList(), emptyList(), emptyList(), emptyList())))),
                        emptyList<DiscoveredTarget>()),
                // Empty solution
                arrayOf(
                        sequenceOf(Solution(emptyList())),
                        emptyList<DiscoveredTarget>()),
                // Empty solutions
                arrayOf(
                        emptySequence<Solution>(),
                        emptyList<DiscoveredTarget>()),
                // Skip test project from proj when it uses in some solution
                arrayOf(
                        sequenceOf(Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk"))))), Solution(listOf(Project("dir/mypro.proj", emptyList(), emptyList(), emptyList(), listOf(Reference("Microsoft.NET.Test.Sdk")))), "dir2\\my.sln")),
                        listOf(DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Restore.id, DotnetConstants.PARAM_PATHS to "dir2/my.sln")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Build.id, DotnetConstants.PARAM_PATHS to "dir2/my.sln")), DiscoveredTarget(mapOf(DotnetConstants.PARAM_COMMAND to DotnetCommandType.Test.id, DotnetConstants.PARAM_PATHS to "dir2/my.sln")))))
    }

    @Test(dataProvider = "testData")
    fun shouldDiscoveryAndGenerateCommands(solutions: Sequence<Solution>, expectedTargets: List<DiscoveredTarget>) {
        // Given
        val paths = sequenceOf("dir1/proj1.csproj", "dir2/proj2.json")
        val ctx = Mockery()
        val solutionDiscover = ctx.mock(SolutionDiscover::class.java)
        val streamFactory = ctx.mock(StreamFactory::class.java)

        ctx.checking(object : Expectations() {
            init {
                oneOf<SolutionDiscover>(solutionDiscover).discover(streamFactory, paths)
                will(returnValue(solutions))
            }
        })

        val discoveryExtension = DotnetRunnerDiscoveryExtension(solutionDiscover)

        // When
        val actualTargets = discoveryExtension.discover(streamFactory, paths).toList()

        // Then
        Assert.assertEquals(actualTargets, expectedTargets)
    }
}
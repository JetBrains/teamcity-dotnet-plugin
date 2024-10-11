package jetbrains.buildServer.dotnet.test.dotnet.commands.transformation

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.cache.depcache.DependencyCache
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.depcache.DotnetDependencyCacheManager
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.ListPackageCommand
import jetbrains.buildServer.dotnet.commands.NugetLocalsCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.transformation.DepCacheRestorationCommandTransformer
import jetbrains.buildServer.dotnet.commands.transformation.DepCacheRestorationCommandTransformer.Companion.MinDotNetSdkVersionForDepCache
import jetbrains.buildServer.dotnet.commands.transformation.DotnetCommandsTransformationStage
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DepCacheRestorationCommandTransformerTest {
    @MockK
    private lateinit var _parametersService: ParametersService
    @MockK
    private lateinit var _nugetLocalsCommand: NugetLocalsCommand
    @MockK
    private lateinit var _listPackageCommand: ListPackageCommand
    @MockK
    private lateinit var _dotnetDepCacheManager: DotnetDependencyCacheManager
    @MockK
    private lateinit var _buildStepContext: BuildStepContext

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        every { _nugetLocalsCommand.commandType } returns DotnetCommandType.NuGetLocals
        every { _listPackageCommand.commandType } returns DotnetCommandType.ListPackage
        val runnerContext = mockk<BuildRunnerContext>()
        every { _buildStepContext.runnerContext } returns runnerContext
        val build = mockk<AgentRunningBuild>()
        every { runnerContext.build } returns build
        val buildFeature = mockk<AgentBuildFeature>()
        every { build.getBuildFeaturesOfType(any()) } returns listOf(buildFeature)
    }

    @Test
    fun `should be on DepCacheRestoration stage`() {
        // arrange
        val transformer = create()

        // act
        val result = transformer.stage

        // assert
        Assert.assertEquals(result, DotnetCommandsTransformationStage.DepCacheRestoration)
    }

    @Test
    fun `should wrap initial commands into auxiliary ones for dependency cache`() {
        // arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, "teamcity.internal.depcache.buildFeature.dotnet.enabled") } returns "true"

        val context = mockk<DotnetCommandContext>()
        every { context.toolVersion } returns MinDotNetSdkVersionForDepCache

        val (command1, command2) = Pair(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        every { command1.commandType } returns DotnetCommandType.Build
        every { command1.targetArguments } returns emptySequence()
        every { command2.commandType } returns DotnetCommandType.Test
        every { command2.targetArguments } returns emptySequence()

        val transformer = create()

        // act
        val result = transformer.apply(context, sequenceOf(command1, command2)).toList()

        // assert: there are 2 initial commands + 2 auxiliary ones per initial
        Assert.assertEquals(result.size, 6)
        Assert.assertEquals(result.get(0).commandType, DotnetCommandType.NuGetLocals) // NuGetLocals auxiliary command for the initial command 1
        Assert.assertEquals(result.get(1), command1) // initial command 1
        Assert.assertEquals(result.get(2).commandType, DotnetCommandType.ListPackage) // ListPackage auxiliary command for the initial command 1
        Assert.assertEquals(result.get(3).commandType, DotnetCommandType.NuGetLocals) // NuGetLocals auxiliary command for the initial command 2
        Assert.assertEquals(result.get(4), command2) // initial command 2
        Assert.assertEquals(result.get(5).commandType, DotnetCommandType.ListPackage) // ListPackage auxiliary command for the initial command 2
    }

    @DataProvider(name = "incompatibleDotnetCommandTypes")
    fun getIncompatibleDotnetCommandTypes(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(DotnetCommandType.VisualStudio),
            arrayOf(DotnetCommandType.Custom),
            arrayOf(DotnetCommandType.VSTest),
            arrayOf(DotnetCommandType.NuGetPush),
            arrayOf(DotnetCommandType.NuGetDelete)
        )
    }
    @Test(dataProvider = "incompatibleDotnetCommandTypes")
    fun `should return initial command when command is not dep cache compatible`(incompatibleCommandType: DotnetCommandType) {
        // arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, "teamcity.internal.depcache.buildFeature.dotnet.enabled") } returns "true"

        val context = mockk<DotnetCommandContext>()
        every { context.toolVersion } returns MinDotNetSdkVersionForDepCache

        val command = mockk<DotnetCommand>()
        every { command.commandType } returns incompatibleCommandType
        every { command.targetArguments } returns emptySequence()

        val transformer = create()

        // act
        val result = transformer.apply(context, sequenceOf(command)).toList()

        // assert: there is only an initial command
        Assert.assertEquals(result.size, 1)
        Assert.assertEquals(result.get(0).commandType, incompatibleCommandType)
    }

    @DataProvider(name = "incompatibleTargets")
    fun getIncompatibleTargets(): Array<Array<Any>> {
        return arrayOf(
            arrayOf("1.exe"),
            arrayOf("prj.dll")
        )
    }
    @Test(dataProvider = "incompatibleTargets")
    fun `should return initial command when target arguments are not dep cache compatible`(incompatibleTarget: String) {
        // arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, "teamcity.internal.depcache.buildFeature.dotnet.enabled") } returns "true"

        val context = mockk<DotnetCommandContext>()
        every { context.toolVersion } returns MinDotNetSdkVersionForDepCache

        val command = mockk<DotnetCommand>()
        every { command.commandType } returns DotnetCommandType.Build
        every { command.targetArguments } returns sequenceOf(TargetArguments(sequenceOf(CommandLineArgument(incompatibleTarget))))

        val transformer = create()

        // act
        val result = transformer.apply(context, sequenceOf(command)).toList()

        // assert: there is only an initial command
        Assert.assertEquals(result.size, 2)
        Assert.assertEquals(result.get(0).commandType, DotnetCommandType.NuGetLocals) // NuGetLocals auxiliary command for the initial build command
        Assert.assertEquals(result.get(1).commandType, DotnetCommandType.Build) // the initial command
    }

    @Test
    fun `should return initial commands when dotnet dependency cache feature is disabled`() {
        // arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, "teamcity.internal.depcache.buildFeature.dotnet.enabled") } returns "false"
        val context = mockk<DotnetCommandContext>()
        every { context.toolVersion } returns MinDotNetSdkVersionForDepCache
        val (command1, command2) = Pair(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        val transformer = create()

        // act
        val result = transformer.apply(context, sequenceOf(command1, command2)).toList()

        // assert
        Assert.assertEquals(result.size, 2)
        Assert.assertEquals(result.get(0), command1) // initial command 1
        Assert.assertEquals(result.get(1), command2) // initial command 2
    }

    @Test
    fun `should return initial commands when tool version is less that minimal required`() {
        // arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, "teamcity.internal.depcache.buildFeature.dotnet.enabled") } returns "true"
        val context = mockk<DotnetCommandContext>()
        every { context.toolVersion } returns Version(2, 1, 105)
        val dependencyCache = mockk<DependencyCache>(relaxed = true)
        every { _dotnetDepCacheManager.cache } returns dependencyCache
        val (command1, command2) = Pair(mockk<DotnetCommand>(), mockk<DotnetCommand>())
        val transformer = create()

        // act
        val result = transformer.apply(context, sequenceOf(command1, command2)).toList()

        // assert
        Assert.assertEquals(result.size, 2)
        Assert.assertEquals(result.get(0), command1) // initial command 1
        Assert.assertEquals(result.get(1), command2) // initial command 2
    }

    private fun create() =  DepCacheRestorationCommandTransformer(
        _parametersService, _nugetLocalsCommand, _listPackageCommand,_dotnetDepCacheManager, _buildStepContext
    )
}
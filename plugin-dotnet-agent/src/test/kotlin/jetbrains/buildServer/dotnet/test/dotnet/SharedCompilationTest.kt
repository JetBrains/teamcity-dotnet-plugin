package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.TargetRegistry
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class SharedCompilationTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                // UNIX or MAC and SDK v. > 2.1.105
                // Prevents the case when VBCSCompiler service remains in memory after `dotnet build` for Linux and consumes 100% of 1 CPU core and a lot of memory
                arrayOf(OSType.UNIX, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 200)), true),
                arrayOf(OSType.MAC, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 200)), true),
                arrayOf(OSType.UNIX, sequenceOf(TargetType.Tool), sequenceOf(Version(2, 1, 300)), true),
                arrayOf(OSType.MAC, sequenceOf(TargetType.Tool), sequenceOf(Version(2, 1, 300)), true),
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 200)), false),
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.Tool), sequenceOf(Version(2, 1, 300)), false),
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.Tool), emptySequence<Version>(), false),

                // SDK v. <= 2.1.105
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 105)), false),
                arrayOf(OSType.UNIX, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 105)), false),
                arrayOf(OSType.MAC, sequenceOf(TargetType.Tool), sequenceOf(Version(2, 1, 105)), false),
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 0, 0)), false),
                arrayOf(OSType.UNIX, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 0, 0)), false),
                arrayOf(OSType.MAC, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 0, 0)), false),
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(1, 0, 1)), false),
                arrayOf(OSType.UNIX, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(1, 0, 1)), false),
                arrayOf(OSType.MAC, sequenceOf(TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(1, 0, 1)), false),

                // WINDOWS and CodeCoverageProfiler and SDK v. > 2.1.105
                // dotCover is waiting for finishing of all spawned processes including a build's infrastructure processes
                // https://github.com/JetBrains/teamcity-dotnet-plugin/issues/132
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool), sequenceOf(Version(2, 1, 300)), true),
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 200)), true),
                arrayOf(OSType.WINDOWS, sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 105)), false),
                arrayOf(OSType.UNIX, sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 300)), true),
                arrayOf(OSType.MAC, sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(1, 0, 1)), false),
                arrayOf(OSType.UNIX, sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 300)), true),
                arrayOf(OSType.MAC, sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool), sequenceOf(Version(1, 0, 1), Version(2, 1, 300)), true),
                arrayOf(OSType.MAC, sequenceOf(TargetType.CodeCoverageProfiler), emptySequence<Version>(), false)
        )
    }

    @Test(dataProvider = "testData")
    fun shouldDefineSuppressing(
            os: OSType,
            activeTargets: Sequence<TargetType>,
            versions: Sequence<Version>,
            expectedRequireSuppressing: Boolean) {
        // Given
        val ctx = Mockery()
        val environment = ctx.mock(Environment::class.java)
        val targetRegistry = ctx.mock(TargetRegistry::class.java)
        val context = DotnetBuildContext(ctx.mock(DotnetCommand::class.java), null, versions.map { DotnetSdk(File("targetPath"), it) }.toSet())
        val sharedCompilation = SharedCompilationImpl(environment, targetRegistry)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(environment).os
                will(returnValue(os))

                oneOf<TargetRegistry>(targetRegistry).activeTargets
                will(returnValue(activeTargets))
            }
        })

        val actualRequireSuppressing = sharedCompilation.requireSuppressing(context)

        // Then
        Assert.assertEquals(actualRequireSuppressing, expectedRequireSuppressing)
    }
}
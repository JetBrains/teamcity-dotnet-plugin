package jetbrains.buildServer.dotnet.test.agent.runner

import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.TargetRegistryImpl
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.use
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TargetRegistryTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool),
                        setOf(TargetType.CodeCoverageProfiler, TargetType.Tool)),
                arrayOf(
                        sequenceOf(TargetType.CodeCoverageProfiler, TargetType.Tool, TargetType.CodeCoverageProfiler, TargetType.CodeCoverageProfiler),
                        setOf(TargetType.CodeCoverageProfiler, TargetType.Tool)),
                arrayOf(
                        emptySequence<TargetType>(),
                        emptySet<TargetType>()))
    }

    @Test(dataProvider = "testData")
    fun shouldActivateTarget(targetsToActivate: Sequence<TargetType>, expectedActiveTargets: Set<TargetType>) {
        // Given
        val targetRegistry = TargetRegistryImpl()

        // When
        val tokens = targetsToActivate.map { targetRegistry.register(it) }.toList()
        val actualActiveTargets = targetRegistry.activeTargets.toSet()
        tokens.reversed().forEach { it.dispose() }

        // Then
        Assert.assertEquals(actualActiveTargets, expectedActiveTargets)
    }

    @Test
    fun shouldDeactivateTarget() {
        // Given
        val targetRegistry = TargetRegistryImpl()

        // When
        disposableOf(targetRegistry.register(TargetType.CodeCoverageProfiler))
        .use {
            disposableOf(targetRegistry.register(TargetType.Tool)).use {
            }

            // Then
            Assert.assertEquals(targetRegistry.activeTargets.count(), 1)
        }

        Assert.assertEquals(targetRegistry.activeTargets.count(), 0)
    }
}
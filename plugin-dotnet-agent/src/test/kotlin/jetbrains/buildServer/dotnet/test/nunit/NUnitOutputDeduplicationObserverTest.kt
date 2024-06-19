package jetbrains.buildServer.dotnet.test.nunit

import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultError
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.messages.serviceMessages.TestStdErr
import jetbrains.buildServer.messages.serviceMessages.TestStdOut
import jetbrains.buildServer.messages.serviceMessages.TestSuiteFinished
import jetbrains.buildServer.messages.serviceMessages.TestSuiteStarted
import jetbrains.buildServer.nunit.NUnitOutputDeduplicationObserver
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class NUnitOutputDeduplicationObserverTest {
    @Test
    fun `should suppress duplicated nunit test standard and error output events`() {
        // arrange
        val events = listOf(
            CommandResultOutput("regular output"),
            CommandResultOutput(TestSuiteStarted("suite1").asString()),
            // should be suppressed
            CommandResultOutput("duplicated output during test"),
            CommandResultOutput(TestStdOut("test1", "duplicated output during test").asString()),
            // should be suppressed
            CommandResultError("duplicated error during test"),
            CommandResultOutput(TestStdErr("test1", "duplicated error during test").asString()),

            CommandResultOutput(TestSuiteFinished("suite1").asString())
        )

        // act
        val observer = NUnitOutputDeduplicationObserver()
        events.forEach { observer.onNext(it) }

        // assert
        val suppressedOut = events
            .filterIsInstance<CommandResultOutput>()
            .firstOrNull { it.attributes.contains(CommandResultAttribute.Suppressed) }
        assertEquals(suppressedOut?.output, "duplicated output during test")

        val suppressedErr = events
            .filterIsInstance<CommandResultError>()
            .firstOrNull { it.attributes.contains(CommandResultAttribute.Suppressed) }
        assertEquals(suppressedErr?.error, "duplicated error during test")
    }
}
package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.InspectCodeConstants
import jetbrains.buildServer.inspect.InspectionOutputObserver
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class InspectionOutputObserverTest {
    @MockK private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _loggerService.writeErrorOutput(any()) } returns Unit
    }

    @DataProvider(name = "notifyCases")
    fun getNotifyCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("No files to inspect were found.", true),
                arrayOf("  No files to inspect were found.  ", true),
                arrayOf("  no files TO inspect were found.", false),
                arrayOf("files to inspect were found.", false),
                arrayOf("   ", false),
                arrayOf("", false)
        )
    }

    @Test(dataProvider = "notifyCases")
    fun shouldNotify(line: String, hasNotification: Boolean) {
        // Given
        val observer = createInstance()

        // When
        observer.onNext(line)

        // Then
        if(hasNotification) {
            every { _loggerService.writeErrorOutput("${InspectionOutputObserver.NoFiles} If you have C++ projects in your solution, specify the x86 ReSharper CLT platform in the ${InspectCodeConstants.RUNNER_DISPLAY_NAME} build step.") }
        }
    }

    private fun createInstance() =
            InspectionOutputObserver(_loggerService)
}
/*
 * Copyright 2000-2023 JetBrains s.r.o.
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
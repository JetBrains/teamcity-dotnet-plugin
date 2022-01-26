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

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.BuildProblemTypes
import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.NugetResultsObserver
import jetbrains.buildServer.dotnet.NugetResultsObserver.Companion.ErrorPrefix
import jetbrains.buildServer.dotnet.NugetResultsObserver.Companion.WarningPrefix
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class NugetResultsObserverTest {
    @MockK private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldWriteBuildProblemWhenHasErrorPrefix() {
        // Given
        val observer = createInstance()
        every { _loggerService.writeBuildProblem(any(), any(), any()) } returns Unit
        val event = CommandResultOutput("${ErrorPrefix}some error")

        // When
        observer.onNext(event)

        // Then
        Assert.assertTrue(event.attributes.contains(CommandResultAttribute.Suppressed))
        verify { _loggerService.writeBuildProblem("some error", BuildProblemTypes.TC_ERROR_MESSAGE_TYPE, "some error") }
    }

    @Test
    fun shouldWriteBuildProblemWhenErrorIsEmpty() {
        // Given
        val observer = createInstance()
        val event = CommandResultOutput(ErrorPrefix)

        // When
        observer.onNext(event)

        // Then
        Assert.assertFalse(event.attributes.contains(CommandResultAttribute.Suppressed))
    }

    @Test
    fun shouldWriteErrorOutputWhenErrorIsBlank() {
        // Given
        val observer = createInstance()
        every { _loggerService.writeErrorOutput(any()) } returns Unit
        val event = CommandResultOutput("${ErrorPrefix}  ")

        // When
        observer.onNext(event)

        // Then
        Assert.assertTrue(event.attributes.contains(CommandResultAttribute.Suppressed))
        verify { _loggerService.writeErrorOutput("  ") }
    }

    @Test
    fun shouldWriteErrorOutputWhenHasSpacesAtStart() {
        // Given
        val observer = createInstance()
        every { _loggerService.writeErrorOutput(any()) } returns Unit
        val event = CommandResultOutput("${ErrorPrefix}  some error")

        // When
        observer.onNext(event)

        // Then
        Assert.assertTrue(event.attributes.contains(CommandResultAttribute.Suppressed))
        verify { _loggerService.writeErrorOutput("  some error") }
    }

    @Test
    fun shouldWriteWarningWhenHasErrorWarning() {
        // Given
        val observer = createInstance()
        every { _loggerService.writeWarning(any()) } returns Unit
        val event = CommandResultOutput("${WarningPrefix}some warning")

        // When
        observer.onNext(event)

        // Then
        Assert.assertTrue(event.attributes.contains(CommandResultAttribute.Suppressed))
        verify { _loggerService.writeWarning("some warning") }
    }

    private fun createInstance() = NugetResultsObserver(_loggerService)
}
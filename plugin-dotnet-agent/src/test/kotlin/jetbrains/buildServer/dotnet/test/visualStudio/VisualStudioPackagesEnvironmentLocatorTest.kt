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

package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.visualStudio.VisualStudioPackagesEnvironmentLocator
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class VisualStudioPackagesEnvironmentLocatorTest {
    @MockK private lateinit var _environment: Environment

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideVisualStudioPath() {
        // Given

        // When
        every { _environment.tryGetVariable("ProgramData") } returns "programDt"
        val locator = createInstance()
        val actualPath = locator.tryGetPackagesPath()

        // Then
        Assert.assertEquals(actualPath, File(File("programDt"), "Microsoft/VisualStudio/Packages").path)
    }

    @Test
    fun shouldNotProvideVisualStudioPathWhenEnvVarIsNull() {
        // Given

        // When
        every { _environment.tryGetVariable("ProgramData") } returns null
        val locator = createInstance()
        val actualPath = locator.tryGetPackagesPath()

        // Then
        Assert.assertNull(actualPath)
    }

    private fun createInstance() =
            VisualStudioPackagesEnvironmentLocator(_environment)
}
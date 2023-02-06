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

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitTests

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.commands.test.splitTests.TestsListTempFile
import jetbrains.buildServer.dotnet.commands.test.splitTests.TestsListTempFileFactory
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File

class TestsListTempFileFactoryTests {
    @MockK
    private lateinit var _pathsServiceMock: PathsService

    @BeforeClass
    fun beforeAll() = MockKAnnotations.init(this)

    @Test
    fun `should create new temp test list`() {
        // arrange
        val fileMock = mockk<File>()
        every { _pathsServiceMock.getTempFileName(any()) } returns fileMock
        val factory = createFactory()

        // act
        val result = factory.new()

        // assert
        Assert.assertNotNull(result)
        Assert.assertTrue(result is TestsListTempFile)
        verify (exactly = 1) { _pathsServiceMock.getTempFileName(".dotnet-tests-list") }
    }

    private fun createFactory() = TestsListTempFileFactory(_pathsServiceMock)
}
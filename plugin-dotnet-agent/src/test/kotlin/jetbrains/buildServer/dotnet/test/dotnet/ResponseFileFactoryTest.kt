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

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.ResponseFileFactoryImpl.Companion.BOM
import jetbrains.buildServer.dotnet.ResponseFileFactoryImpl.Companion.BlockName
import jetbrains.buildServer.dotnet.ResponseFileFactoryImpl.Companion.ResponseFileExtension
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.rx.Disposable
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.InputStreamReader

class ResponseFileFactoryTest {
    @MockK private lateinit var _pathService: PathsService
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _msBuildParameterConverter: MSBuildParameterConverter

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { arg<String>(0)}
    }

    @Test
    fun shouldCreateResponseFile() {
        // Given
        val rspFileName = "rspFile"
        val rspFile = File(rspFileName)
        val fileSystemService = VirtualFileSystemService()
        val responseFileFactory = createInstance(fileSystemService)

        every { _pathService.getTempFileName("abc" + ResponseFileExtension) } returns File(rspFileName)
        val blockToken = mockk<Disposable> {
            every { dispose() } returns Unit
        }

        every { _loggerService.writeBlock(BlockName + " abc") } returns blockToken
        every { _loggerService.writeStandardOutput(any(), Color.Details) } returns Unit
        every { _msBuildParameterConverter.convert(any()) } answers {
            arg<Sequence<MSBuildParameter>>(0).map { "${it.name}=${it.value}" }
        }

        // When
        val actualRepsonseFile = responseFileFactory.createResponeFile(
                "abc",
                sequenceOf(CommandLineArgument("arg1"), CommandLineArgument("arg2")),
                sequenceOf(MSBuildParameter("par1", "val1"), MSBuildParameter("par2", "val2")),
                Verbosity.Detailed)

        // Then
        verify { blockToken.dispose() }
        fileSystemService.read(rspFile) {
            val actualBOM: ByteArray = byteArrayOf(0, 0, 0)
            it.read(actualBOM, 0,3)
            Assert.assertEquals(actualBOM, BOM)
            InputStreamReader(it).use {
                Assert.assertEquals(it.readLines(), listOf(
                        "arg1",
                        "arg2",
                        "par1=val1",
                        "par2=val2"
                ))
            }
        }
        Assert.assertEquals(actualRepsonseFile, Path(rspFileName))
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            ResponseFileFactoryImpl(
                _pathService,
                fileSystemService,
                _loggerService,
                _msBuildParameterConverter,
                _virtualContext)
}
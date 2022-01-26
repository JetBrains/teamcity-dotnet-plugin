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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.dotnet.DotnetToolResolver
import jetbrains.buildServer.dotnet.DotnetVersionProviderImpl
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetVersionProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _commandLineExecutor: CommandLineExecutor
    private lateinit var _versionParser: VersionParser
    private lateinit var _dotnetToolResolver: DotnetToolResolver
    private lateinit var _buildStepContext: BuildStepContext

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _versionParser = _ctx.mock(VersionParser::class.java)
        _dotnetToolResolver = _ctx.mock(DotnetToolResolver::class.java)
        _buildStepContext = _ctx.mock(BuildStepContext::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("3.0.100-preview9-014004"), emptyList<String>(), 0, Version(2, 2, 202)),
                arrayOf(listOf("3.0.100-preview9-014004"), emptyList<String>(), 1, Version.Empty),
                arrayOf(listOf("3.0.100-preview9-014004"), emptyList<String>(), -2, Version.Empty),
                arrayOf(listOf("3.0.100-preview9-014004"), listOf("some error"), 0, Version.Empty),
                arrayOf(emptyList<String>(), listOf("some error"), 0, Version.Empty),
                arrayOf(emptyList<String>(), listOf("some error"), 1, Version.Empty)
                )
    }

    @Test(dataProvider = "testData")
    fun shouldGetDotnetVersion(stdOutVersion: Collection<String>, stdErr: Collection<String>, exitCode: Int, expectedVersion: Version) {
        // Given
        val workingDirectoryPath = Path("wd")
        val toolPath = Path("dotnet")
        val versionCommandline = CommandLine(
                null,
                TargetType.Tool,
                toolPath,
                workingDirectoryPath,
                DotnetVersionProviderImpl.versionArgs,
                emptyList())

        val getVersionResult = CommandLineResult(exitCode, stdOutVersion, stdErr)
        _ctx.checking(object : Expectations() {
            init {
                oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(versionCommandline)
                will(returnValue(getVersionResult))

                allowing<VersionParser>(_versionParser).parse(stdOutVersion)
                will(returnValue(Version(2, 2, 202)))
            }
        })

        val fileSystemService = VirtualFileSystemService()
        val dotnetVersionProvider = createInstance(fileSystemService)

        // When
        val actualVersion = dotnetVersionProvider.getVersion(toolPath, workingDirectoryPath)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualVersion, expectedVersion)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetVersionProviderImpl(
                    _buildStepContext,
                    _commandLineExecutor,
                    _versionParser,
                    fileSystemService,
                    _dotnetToolResolver)
}
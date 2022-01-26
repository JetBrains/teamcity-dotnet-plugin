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

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.VSTestLoggerEnvironmentBuilder
import jetbrains.buildServer.dotnet.VSTestLoggerEnvironmentCleaner
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class VSTestLoggerEnvironmentCleanerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _fileSystemService: FileSystemService
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock(PathsService::class.java)
        _fileSystemService = _ctx.mock(FileSystemService::class.java)
        _loggerService = _ctx.mock(LoggerService::class.java)
    }

    @Test
    fun shouldClean() {
        // Given
        val checkoutDir = File("checkoutDir")
        val loggerDir1 = File(checkoutDir, "${VSTestLoggerEnvironmentBuilder.directoryPrefix}loggerdir")
        val loggerDir2 = File(checkoutDir, "${VSTestLoggerEnvironmentBuilder.directoryPrefix}2313123")
        val dir1 = File(checkoutDir, "2313123${VSTestLoggerEnvironmentBuilder.directoryPrefix}")
        val dir2 = File(checkoutDir, "2313123${VSTestLoggerEnvironmentBuilder.directoryPrefix}")
        val dir3 = File("abc", "${VSTestLoggerEnvironmentBuilder.directoryPrefix}loggerdir")
        val file1 = File(checkoutDir, "${VSTestLoggerEnvironmentBuilder.directoryPrefix}abc")

        val fileSystemService = VirtualFileSystemService()
                .addDirectory(checkoutDir)
                .addDirectory(loggerDir1)
                .addDirectory(dir1)
                .addDirectory(loggerDir2)
                .addDirectory(dir2)
                .addDirectory(dir3)
                .addFile(file1)

        val environmentCleaner = VSTestLoggerEnvironmentCleaner(
                _pathService,
                fileSystemService,
                _loggerService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDir))
            }
        })

        environmentCleaner.clean()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(fileSystemService.isExists(checkoutDir), true)
        Assert.assertEquals(fileSystemService.isExists(loggerDir1), false)
        Assert.assertEquals(fileSystemService.isExists(loggerDir2), false)
        Assert.assertEquals(fileSystemService.isExists(dir1), true)
        Assert.assertEquals(fileSystemService.isExists(dir2), true)
        Assert.assertEquals(fileSystemService.isExists(dir3), true)
        Assert.assertEquals(fileSystemService.isExists(file1), true)
    }

    @Test
    fun shouldLogError() {
        // Given
        val checkoutDir = File("checkoutDir")
        val loggerDir1 = File(checkoutDir, "${VSTestLoggerEnvironmentBuilder.directoryPrefix}loggerdir")
        val error = Exception("some error")

        val fileSystemService = VirtualFileSystemService()
                .addDirectory(checkoutDir)
                .addDirectory(loggerDir1, VirtualFileSystemService.errorOnRemove(error))

        val environmentCleaner = VSTestLoggerEnvironmentCleaner(
                _pathService,
                fileSystemService,
                _loggerService)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDir))

                oneOf<LoggerService>(_loggerService).writeErrorOutput("Failed to remove logger directory \"$loggerDir1\"")
            }
        })

        environmentCleaner.clean()

        // Then
        _ctx.assertIsSatisfied()
    }
}
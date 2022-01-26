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

package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotcover.EnvironmentVariablesImpl
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class EnvironmentVariablesTest {
    @MockK private lateinit var  _virtualContext: VirtualContext;

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "defaultVars")
    fun osTypesData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.UNIX, EnvironmentVariablesImpl.linuxDefaultVariables),
                arrayOf(OSType.MAC, emptySequence<CommandLineEnvironmentVariable>()),
                arrayOf(OSType.WINDOWS, emptySequence<CommandLineEnvironmentVariable>()))
    }

    @Test(dataProvider = "defaultVars")
    fun shouldProvideLinuxDefaultVarsWhenLinux(os: OSType, expectedVariables: Sequence<CommandLineEnvironmentVariable>) {
        // Given
        val environmentVariables = createInstance()

        // When
        every { _virtualContext.targetOSType } returns os
        val actualVariables = environmentVariables.getVariables().toList()

        // Then
        Assert.assertEquals(actualVariables, expectedVariables.toList())
    }

      private fun createInstance() = EnvironmentVariablesImpl(_virtualContext)
}
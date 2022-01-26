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
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetFrameworksProviderTest {
    @MockK private lateinit var _windowsRegistry: WindowsRegistry
    @MockK private lateinit var _dotnetFrameworkValidator: DotnetFrameworkValidator
    @MockK private lateinit var _visitor1: DotnetFrameworksWindowsRegistryVisitor
    @MockK private lateinit var _visitor2: DotnetFrameworksWindowsRegistryVisitor

    private val _key1 = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.LOCAL_MACHINE,"SOFTWARE", "Microsoft", "1")
    private val _key2 = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness32, WindowsRegistryHive.LOCAL_MACHINE,"SOFTWARE", "Microsoft", "2")
    private val _key3 = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.LOCAL_MACHINE,"SOFTWARE", "Microsoft", "3")

    private val _framework1 = DotnetFramework(Platform.x86, Version(1), File("a"))
    private val _framework2 = DotnetFramework(Platform.x64, Version(2), File("b"))
    private val _framework3 = DotnetFramework(Platform.x64, Version(3), File("c"))
    private val _framework4 = DotnetFramework(Platform.x64, Version(2), File("b"))
    private val _framework5 = DotnetFramework(Platform.x64, Version(5), File("e"))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideFrameworks() {
        // Given
        val frameworksProvider = createInstance()

        // When
        every { _visitor1.keys } returns sequenceOf(_key1, _key2)
        every { _visitor2.keys } returns sequenceOf(_key2, _key3)

        every { _visitor1.getFrameworks() } returns sequenceOf(_framework1, _framework2)
        every { _visitor2.getFrameworks() } returns sequenceOf(_framework2, _framework3, _framework4, _framework5)

        every { _windowsRegistry.accept(_key1, _visitor1, true) } returns Unit
        every { _windowsRegistry.accept(_key2, _visitor1, true) } returns Unit
        every { _windowsRegistry.accept(_key2, _visitor2, true) } returns Unit
        every { _windowsRegistry.accept(_key3, _visitor2, true) } returns Unit

        every { _dotnetFrameworkValidator.isValid(_framework1) } returns true
        every { _dotnetFrameworkValidator.isValid(_framework2) } returns true
        every { _dotnetFrameworkValidator.isValid(_framework3) } returns false
        every { _dotnetFrameworkValidator.isValid(_framework4) } returns true
        every { _dotnetFrameworkValidator.isValid(_framework5) } returns true

        // Then
        Assert.assertEquals(
                frameworksProvider.getFrameworks().sortedBy { it.toString() }.toList(),
                listOf(_framework1, _framework2, _framework5).sortedBy { it.toString() }.toList())

        verify(exactly = 1) { _windowsRegistry.accept(_key1, _visitor1, true) }
        verify(exactly = 1) { _windowsRegistry.accept(_key2, _visitor1, true) }
        verify(exactly = 1) { _windowsRegistry.accept(_key2, _visitor2, true) }
        verify(exactly = 1) { _windowsRegistry.accept(_key3, _visitor2, true) }
    }

    private fun createInstance() =
            DotnetFrameworksProviderImpl(
                    _windowsRegistry,
                    listOf(_visitor1, _visitor2),
                    _dotnetFrameworkValidator)
}
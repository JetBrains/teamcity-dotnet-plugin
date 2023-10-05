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

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import jetbrains.buildServer.dotnet.ComposedTestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ComposedTestsFilterProviderTest {
    @DataProvider(name = "testData")
    fun testData(): Any {
        val abc = mockk<TestsFilterProvider>();
        every { abc.getFilterExpression(any()) } returns "Abc"
        val qw = mockk<TestsFilterProvider>()
        every { qw.getFilterExpression(any()) } returns "Qw"
        val xyz = mockk<TestsFilterProvider>()
        every { xyz.getFilterExpression(any()) } returns "Xyz"
        val blank = mockk<TestsFilterProvider>()
        every { blank.getFilterExpression(any()) } returns "  "
        val empty = mockk<TestsFilterProvider>()
        every { empty.getFilterExpression(any()) } returns ""

        return arrayOf(
                arrayOf(emptyList<String>(), ""),
                arrayOf(listOf(abc), "Abc"),
                arrayOf(listOf(empty), ""),
                arrayOf(listOf(blank), ""),
                arrayOf(listOf(blank, empty), ""),
                arrayOf(listOf(abc, xyz), "(Abc) & (Xyz)"),
                arrayOf(listOf(blank, xyz), "Xyz"),
                arrayOf(listOf(blank, xyz, empty), "Xyz"),
                arrayOf(listOf(abc, blank, xyz), "(Abc) & (Xyz)"),
                arrayOf(listOf(abc, blank, xyz, qw, empty), "(Abc) & (Xyz) & (Qw)")
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFilter(testsFilterProviders: List<TestsFilterProvider>, expecedFilter: String) {
        // Given
        val provider = createInstance(testsFilterProviders)

        // When
        val actualFilter = provider.getFilterExpression(TestsSplittingMode.TestClassNameFilter)

        // Then
        Assert.assertEquals(actualFilter, expecedFilter)
    }

    private fun createInstance(testsFilterProviders: List<TestsFilterProvider>) = ComposedTestsFilterProvider(testsFilterProviders)
}
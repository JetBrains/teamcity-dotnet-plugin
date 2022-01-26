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

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.discovery.DefaultDiscoveredTargetNameFactory
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DefaultDiscoveredTargetNameFactoryTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(DotnetCommandType.Build, "dir/cs.proj", "build dir/cs.proj"),
                arrayOf(DotnetCommandType.Build, "abc cs.proj", "build \"abc cs.proj\""),
                arrayOf(DotnetCommandType.Build, "cs.proj", "build cs.proj"),
                arrayOf(DotnetCommandType.NuGetPush, "dir/cs.proj", "nuget push dir/cs.proj"))
    }

    @Test(dataProvider = "testData")
    fun shouldCreateName(commandType: DotnetCommandType, path: String, expectedName: String) {
        // Given
        val nameFactory = DefaultDiscoveredTargetNameFactory()

        // When
        val actualName = nameFactory.createName(commandType, path)

        // Then
        Assert.assertEquals(actualName, expectedName)
    }
}
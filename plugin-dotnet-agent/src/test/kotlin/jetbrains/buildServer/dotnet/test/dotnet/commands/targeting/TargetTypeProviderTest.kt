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

package jetbrains.buildServer.dotnet.test.dotnet.commands.targeting

import jetbrains.buildServer.dotnet.CommandTargetType
import jetbrains.buildServer.dotnet.commands.targeting.TargetTypeProviderImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TargetTypeProviderTest {
    @DataProvider
    fun cases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(File("Abc.dll"), CommandTargetType.Assembly),
                arrayOf(File("Abc.DLl"), CommandTargetType.Assembly),
                arrayOf(File(File("Path"), "Abc.DLl"), CommandTargetType.Assembly),

                arrayOf(File(File("Path"), "Abc.csproj"), CommandTargetType.Unknown),
                arrayOf(File("Abc.csproj"), CommandTargetType.Unknown),
                arrayOf(File(".csproj"), CommandTargetType.Unknown),
                arrayOf(File("Abc"), CommandTargetType.Unknown),
                arrayOf(File(""), CommandTargetType.Unknown)
        )
    }

    @Test(dataProvider = "cases")
    fun shouldProvideTargetType(file: File, expectedTargetType: CommandTargetType) {
        // Given
        val provider = createInstance()

        // When
        val actualTargetType = provider.getTargetType(file);

        // Then
        Assert.assertEquals(actualTargetType, expectedTargetType)
    }

    private fun createInstance() = TargetTypeProviderImpl()
}
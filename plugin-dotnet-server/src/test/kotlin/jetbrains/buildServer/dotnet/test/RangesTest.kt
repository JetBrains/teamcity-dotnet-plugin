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

import jetbrains.buildServer.Range
import jetbrains.buildServer.combineOf
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class RangesTest {
    @DataProvider
    fun inData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Version(5, 1), Version(5, 0).including() to Version(5, 3).including(), true),
                arrayOf(Version(5, 0), Version(5, 1).including() to Version(5, 3).including(), false),
                arrayOf(Version(5, 0), Version(5, 0).including() to Version(5, 3).including(), true),
                arrayOf(Version(5, 3), Version(5, 0).including() to Version(5, 3).including(), true),
                arrayOf(Version(5, 0), Version(5, 0).excluding() to Version(5, 3).including(), false),
                arrayOf(Version(5, 3), Version(5, 0).including() to Version(5, 3).excluding(), false),
                arrayOf(Version(5, 0), Version(5, 1).excluding() to Version(5, 3).including(), false),
                arrayOf(Version(5, 0), Version(5, 1).including() to Version(5, 3).excluding(), false),
                arrayOf(Version(5, 0), Version(5, 1).excluding() to Version(5, 3).excluding(), false),
                arrayOf(Version(5, 1, 100), Version(5, 0).including() to Version(5, 3).including(), true),
                arrayOf(Version(5, 1, 100), Version(5).including() to Version(6).including(), true),
                arrayOf(Version(3), Version(5).including() to Version(6).including(), false),
                arrayOf(Version(0), Version(5).including() to Version(6).including(), false),
                arrayOf(Version(), Version(5).including() to Version(6).including(), false),

                arrayOf(
                        Version(5, 1),
                        combineOf(
                                Version(5, 0).including() to Version(5, 3).including()
                        ),
                        true),

                arrayOf(
                        Version(6, 1),
                        combineOf(
                                Version(5, 0).including() to Version(5, 3).including(),
                                Version(6, 0).including() to Version(6, 3).including()
                        ),
                        true),

                arrayOf(
                        Version(5, 9),
                        combineOf(
                                Version(5, 0).including() to Version(5, 3).including(),
                                Version(6, 0).including() to Version(6, 3).including()
                        ),
                        false),

                arrayOf(
                        Version(6, 1),
                        combineOf(
                                Version(5, 0).including() to Version(9).including(),
                                Version(6, 0).including() to Version(6, 3).including()
                        ),
                        true)
        )
    }

    @Test(dataProvider = "inData")
    fun shouldParse(version: Version, range: Range<Version>, expectedResult: Boolean) {
        // Given

        // When
        val actualResult = version `in` range

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }
}
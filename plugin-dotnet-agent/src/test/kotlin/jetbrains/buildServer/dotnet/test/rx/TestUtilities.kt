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

package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.materialize
import jetbrains.buildServer.rx.toSequence
import org.testng.Assert

fun <T>assertEquals(actual: Observable<T>, expected: Observable<T>) {
    val actualNotifications = actual.materialize().toSequence(0).toList()
    val expectedNotifications = expected.materialize().toSequence(0).toList()
    Assert.assertEquals(actualNotifications, expectedNotifications)
}
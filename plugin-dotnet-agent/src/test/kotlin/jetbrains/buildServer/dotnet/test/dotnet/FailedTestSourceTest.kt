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

import jetbrains.buildServer.agent.runner.ServiceMessageSource
import jetbrains.buildServer.dotnet.FailedTestSourceImpl
import jetbrains.buildServer.dotnet.test.rx.assertEquals
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.TestFailed
import jetbrains.buildServer.messages.serviceMessages.TestFinished
import jetbrains.buildServer.messages.serviceMessages.TestIgnored
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.rx.NotificationCompleted.Companion.completed
import org.testng.annotations.DataProvider
import org.testng.annotations.Test


class FailedTestSourceTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestFailed("name", "message")), completed()),
                        observableOf(NotificationNext(Unit), completed<ServiceMessage>())),
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestFinished("name", 1)), completed<ServiceMessage>()),
                        observableOf(completed<ServiceMessage>())),
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestIgnored("name", "comment")), completed<ServiceMessage>()),
                        observableOf(completed<ServiceMessage>())),
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestFailed("name", "message")), NotificationNext<ServiceMessage>(TestFailed("name2", "message2")), completed<ServiceMessage>()),
                        observableOf(NotificationNext(Unit), completed<ServiceMessage>())),
                arrayOf(
                        observableOf(completed<ServiceMessage>()),
                        observableOf(completed<ServiceMessage>())))
    }

    @Test(dataProvider = "testData")
    fun shouldMap(data: Observable<Notification<ServiceMessage>>, expectedNotifications: Observable<Notification<Unit>>) {
        // Given
        val serviceMessageSource = object : ServiceMessageSource {
            override fun subscribe(observer: Observer<ServiceMessage>): Disposable =
                    data.dematerialize().subscribe(observer)
        }

        // When
        val actualNotifications = FailedTestSourceImpl(serviceMessageSource)

        // Then
        assertEquals(actualNotifications, expectedNotifications.dematerialize())
    }
}
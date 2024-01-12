

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
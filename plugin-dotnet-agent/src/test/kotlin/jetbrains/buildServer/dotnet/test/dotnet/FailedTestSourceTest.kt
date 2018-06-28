package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.runner.ServiceMessageSource
import jetbrains.buildServer.dotnet.FailedTestSourceImpl
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.TestFailed
import jetbrains.buildServer.messages.serviceMessages.TestFinished
import jetbrains.buildServer.messages.serviceMessages.TestIgnored
import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test


class FailedTestSourceTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestFailed("name", "message")), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext<Unit>(Unit), NotificationCompleted.shared<Unit>())),
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestFinished("name", 1)), NotificationCompleted.shared<Int>()),
                        listOf(NotificationCompleted.shared<Unit>())),
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestIgnored("name", "comment")), NotificationCompleted.shared<Int>()),
                        listOf(NotificationCompleted.shared<Unit>())),
                arrayOf(
                        observableOf(NotificationNext<ServiceMessage>(TestFailed("name", "message")), NotificationNext<ServiceMessage>(TestFailed("name2", "message2")), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext<Unit>(Unit), NotificationCompleted.shared<Unit>())),
                arrayOf(
                        observableOf(NotificationCompleted.shared<Int>()),
                        listOf(NotificationCompleted.shared<Unit>())))
    }

    @Test(dataProvider = "testData")
    fun shouldMap(data: Observable<Notification<ServiceMessage>>, expectedNotifications: List<Notification<Unit>>) {
        // Given
        val serviceMessageSource = object : ServiceMessageSource {
            override fun subscribe(observer: Observer<ServiceMessage>): Disposable =
                data.dematerialize().subscribe(observer)
        }

        // When
        val actualNotifications = FailedTestSourceImpl(serviceMessageSource)

        // Then
        val actual = actualNotifications.materialize().toSequence().toList()
        Assert.assertEquals(
                actual,
                expectedNotifications)
    }
}
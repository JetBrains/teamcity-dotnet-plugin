package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SubjectsTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>()),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>(), NotificationNext(4)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error), NotificationNext(4)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error))),
                arrayOf(
                        emptyObservable<Int>(),
                        emptyList<Int>()))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideData(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<Int>>) {
        // Given
        val subject = subjectOf<Int>()
        val actualNotifications = mutableListOf<Notification<Int>>()
        subject.materialize().subscribe { actualNotifications += it }.use {
            // When
            data.dematerialize().subscribe(subject)

            // Then
            Assert.assertEquals(
                    actualNotifications,
                    expectedNotifications)
        }
    }

    companion object {
        private val error = Exception()
    }

}
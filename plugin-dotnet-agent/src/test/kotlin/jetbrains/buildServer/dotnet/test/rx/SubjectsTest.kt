package jetbrains.buildServer.dotnet.test.rx

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import jetbrains.buildServer.rx.*

class SubjectsTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>(), NotificationNext(4)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(_error)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(_error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(_error), NotificationNext(4)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(_error))),
                arrayOf(
                        emptyObservable<Int>(),
                        emptyList<Int>()))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideData(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<Int>>) {
        // Given
        val subject = subjectOf<Int>()
        val actualNotifications = mutableListOf<Notification<Int>>()
        subject.materialize().subscribe({actualNotifications.add(it)}).use {
            // When
            data.dematerialize().subscribe(subject)

            // Then
            Assert.assertEquals(
                    actualNotifications,
                    expectedNotifications)
        }
    }

    companion object {
        private val _error = Exception()
    }

}
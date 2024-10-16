

package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.Test

class ObserversTest {
    @Test
    fun shouldImplementOnNext() {
        // Given
        var value = 0

        // When
        val observer = observer<Int> { v -> value = v }
        observer.onNext(99)

        // Then
        Assert.assertEquals(value, 99)
    }

    @Test
    fun shouldImplementOnError() {
        // Given
        var exception: Exception? = null

        // When
        val someError = Exception("test")
        val observer = observer<Int> ({ _:Int -> Unit}, { e:Exception -> exception = e }, {})
        observer.onError(someError)

        // Then
        Assert.assertEquals(exception, someError)
    }

    @Test
    fun shouldImplementOnComplete() {
        // Given
        var completed: Boolean? = null

        // When
        val observer = observer<Int> ({ _:Int -> Unit}, { _:Exception -> Unit }, { completed = true })
        observer.onComplete()

        // Then
        Assert.assertEquals(completed, true)
    }

    @Test
    fun shouldConvertCollectionToObsever() {
        // Given
        val values = mutableListOf<Int>()

        // When
        val observer = values.toObserver()
        observer.onNext(1)
        observer.onNext(2)
        observer.onComplete()

        // Then
        Assert.assertEquals(values, listOf(1, 2))
    }

    @Test
    fun shouldDematerialize() {
        // Given
        val values = mutableListOf<Notification<Int>>()

        // When
        val observer = values.toObserver().dematerialize()
        observer.onNext(1)
        observer.onNext(2)

        // Then
        Assert.assertEquals(values, listOf(NotificationNext(1), NotificationNext(2)))
    }
}
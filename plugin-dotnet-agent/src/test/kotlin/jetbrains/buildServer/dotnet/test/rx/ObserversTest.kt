package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.emptyDisposable
import jetbrains.buildServer.rx.observable
import jetbrains.buildServer.rx.observableOf
import jetbrains.buildServer.rx.observer
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
        val observer = observer<Int> ({v:Int -> Unit}, { e:Exception -> exception = e }, {})
        observer.onError(someError)

        // Then
        Assert.assertEquals(exception, someError)
    }

    @Test
    fun shouldImplementOnComplete() {
        // Given
        var completed: Boolean? = null

        // When
        val observer = observer<Int> ({v:Int -> Unit}, { e:Exception -> Unit }, { completed = true })
        observer.onComplete()

        // Then
        Assert.assertEquals(completed, true)
    }
}
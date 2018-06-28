package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test

class DisposablesTest {
    @Test
    fun shouldDisposeWhenAction() {
        // Given
        var isDisposed = false
        val disposable = disposableOf { isDisposed = true }

        // When
        disposable.dispose()

        // Then
        Assert.assertTrue(isDisposed)
    }

    @Test
    fun shouldDisposeOnceWhenSeveralDisposes() {
        // Given
        var disposedCounter = 0
        val disposable = disposableOf { disposedCounter++ }

        // When
        disposable.dispose()
        disposable.dispose()
        disposable.dispose()

        // Then
        Assert.assertEquals(disposedCounter, 1)
    }

    @Test
    fun shouldDisposeWhenComposite() {
        // Given
        val ctx = Mockery()
        val disposable1 = ctx.mock<Disposable>(Disposable::class.java, "1")
        val disposable2 = ctx.mock<Disposable>(Disposable::class.java, "2")
        val disposable3 = ctx.mock<Disposable>(Disposable::class.java, "3")

        ctx.checking(object : Expectations() {
            init {
                oneOf<Disposable>(disposable1).dispose()
                oneOf<Disposable>(disposable2).dispose()
                oneOf<Disposable>(disposable3).dispose()
            }
        })

        val disposable = disposableOf(disposable1, disposable2, disposable3)

        // When
        disposable.dispose()

        // Then
        ctx.assertIsSatisfied()
    }

    @Test
    fun shouldDisposeAfterUse() {
        // Given
        val ctx = Mockery()
        val disposable = ctx.mock<Disposable>(Disposable::class.java, "1")

        ctx.checking(object : Expectations() {
            init {
                oneOf<Disposable>(disposable).dispose()
            }
        })

        // When
        disposable.use { }

        // Then
        ctx.assertIsSatisfied()
    }

    @Test
    fun shouldDisposeAfterUseWhenException() {
        // Given
        val ctx = Mockery()
        val disposable = ctx.mock<Disposable>(Disposable::class.java, "1")

        ctx.checking(object : Expectations() {
            init {
                oneOf<Disposable>(disposable).dispose()
            }
        })

        // When
        try {
            disposable.use { throw Exception() }
        }
        catch (ex: Exception) { }

        // Then
        ctx.assertIsSatisfied()
    }
}
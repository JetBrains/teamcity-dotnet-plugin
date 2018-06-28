package jetbrains.buildServer.dotnet.test.agent.runner

import jetbrains.buildServer.agent.runner.ServiceMessageSourceImpl
import jetbrains.buildServer.messages.serviceMessages.*
import jetbrains.buildServer.rx.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test

class ServiceMessageSourceTest {
    @Test
    fun shouldProvideMessages() {
        // Given
        val testFailed = TestFailed("name", "message")
        val testIgnored = TestIgnored("name", "comment")
        val ctx = Mockery()
        val serviceMessagesRegister = ctx.mock(ServiceMessagesRegister::class.java)
        val source = ServiceMessageSourceImpl(serviceMessagesRegister)
        val actualMessages = mutableListOf<ServiceMessage>()

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<ServiceMessagesRegister>(serviceMessagesRegister).registerHandler(ServiceMessageTypes.TEST_FAILED, source)

                oneOf<ServiceMessagesRegister>(serviceMessagesRegister).removeHandler(ServiceMessageTypes.TEST_FAILED)
            }
        })

        source.subscribe({actualMessages.add(it)}).use {
            source.subscribe({ }).use {
                for (serviceMessage in sequenceOf(testFailed, testIgnored)) {
                    source.handle(serviceMessage)
                }
            }
        }

        // Then
        ctx.assertIsSatisfied()
        Assert.assertEquals(actualMessages, listOf(testFailed, testIgnored))
    }

    fun shouldRegisterAndUnregisterHandlerOnce() {
        // Given
        val ctx = Mockery()
        val serviceMessagesRegister = ctx.mock(ServiceMessagesRegister::class.java)
        val source = ServiceMessageSourceImpl(serviceMessagesRegister)

        // When
        ctx.checking(object : Expectations() {
            init {
                oneOf<ServiceMessagesRegister>(serviceMessagesRegister).registerHandler(ServiceMessageTypes.TEST_FAILED, source)

                oneOf<ServiceMessagesRegister>(serviceMessagesRegister).removeHandler(ServiceMessageTypes.TEST_FAILED)
            }
        })

        source.subscribe({}).use {
            source.subscribe({}).use {
                source.subscribe({ }).use {
                }
            }
        }

        // Then
        ctx.assertIsSatisfied()
    }
}
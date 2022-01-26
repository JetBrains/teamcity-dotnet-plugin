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

package jetbrains.buildServer.dotnet.test.agent.runner

import jetbrains.buildServer.agent.runner.ServiceMessageSourceImpl
import jetbrains.buildServer.messages.serviceMessages.*
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
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

        source.subscribe { actualMessages += it }.use {
            source.subscribe { }.use {
                for (serviceMessage in sequenceOf(testFailed, testIgnored)) {
                    source.handle(serviceMessage)
                }
            }
        }

        // Then
        ctx.assertIsSatisfied()
        Assert.assertEquals(actualMessages, listOf(testFailed, testIgnored))
    }

    @Test
    fun shouldRegisterAndUnregisterHandlerOnce() {
        // Given
        val ctx = Mockery()
        val serviceMessagesRegister = ctx.mock(ServiceMessagesRegister::class.java)
        val source = ServiceMessageSourceImpl(serviceMessagesRegister)

        // When
        ctx.checking(object : Expectations() {
            init {
                ServiceMessageSourceImpl.serviceMessages.forEach {
                    oneOf<ServiceMessagesRegister>(serviceMessagesRegister).registerHandler(it, source)
                    oneOf<ServiceMessagesRegister>(serviceMessagesRegister).removeHandler(it)
                }
            }
        })

        source.subscribe {}.use {
            source.subscribe {}.use {
                source.subscribe { }.use {
                }
            }
        }

        // Then
        ctx.assertIsSatisfied()
    }
}
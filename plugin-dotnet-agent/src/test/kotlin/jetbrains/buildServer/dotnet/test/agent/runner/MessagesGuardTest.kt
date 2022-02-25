@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.MessagesGuard
import jetbrains.buildServer.agent.runner.MessagesGuard.Companion.IndexAttribute
import jetbrains.buildServer.agent.runner.MessagesGuard.Companion.SourceAttribute
import jetbrains.buildServer.agent.runner.Source
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MessagesGuardTest {
    @MockK private lateinit var _messagesSource: Source<String>

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testAddMisingMessages(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(listOf(""), listOf("")),
                arrayOf(listOf("  "), listOf("  ")),
                arrayOf(listOf("Abc"), listOf("Abc")),
                arrayOf(listOf("   Abc  "), listOf("   Abc  ")),
                arrayOf(listOf(createMessage("s1", 0)), listOf(createMessage("s1", 0))),
                arrayOf(listOf(createMessage("s1", 0) + createMessage("s1", 1)), listOf(createMessage("s1", 0), createMessage("s1", 1))),
                arrayOf(listOf(createMessage("s1", 0) + "abc"), listOf(createMessage("s1", 0), "abc")),
                arrayOf(
                        listOf(
                                createMessage("S", null)
                        ),
                        listOf(
                                createMessage("S", null)
                        )),
                arrayOf(
                        listOf(
                                createMessage(null,2)
                        ),
                        listOf(
                                createMessage(null,2)
                        )),
                arrayOf(
                        listOf(
                                createMessage("s0", 0),
                                createMessage("s0", 1),
                                createMessage("s0", 2)
                        ),
                        listOf(
                                createMessage("s0", 0),
                                createMessage("s0", 1),
                                createMessage("s0", 2)
                        )),
                arrayOf(
                        listOf(
                                createMessage("s1", 0),
                                createMessage("s1", 2)
                        ),
                        listOf(
                                createMessage("s1", 0),
                                createMessage("s1", 1),
                                createMessage("s1", 2)
                        )),
                arrayOf(
                        listOf(
                                createMessage("s1", 0) + createMessage("s1", 2)
                        ),
                        listOf(
                                createMessage("s1", 0),
                                createMessage("s1", 1),
                                createMessage("s1", 2)
                        )),
                arrayOf(
                        listOf(
                                createMessage("s1", 0) + "  Abc " + createMessage("s1", 2) + "  Xyz "
                        ),
                        listOf(
                                createMessage("s1", 0),
                                "  Abc ",
                                createMessage("s1", 1),
                                createMessage("s1", 2),
                                "  Xyz "
                        )),
                arrayOf(
                        listOf(
                                createMessage("s2", 0),
                                createMessage("s2", 3)
                        ),
                        listOf(
                                createMessage("s2", 0),
                                createMessage("s2", 1),
                                createMessage("s2", 2),
                                createMessage("s2", 3)
                        )),
                arrayOf(
                        listOf(
                                createMessage("s3", 2)
                        ),
                        listOf(
                                createMessage("s3", 0),
                                createMessage("s3", 1),
                                createMessage("s3", 2)
                        )),
                arrayOf(
                        listOf(
                                createMessage("s5", 0),
                                createMessage("s5", 1),
                                createMessage("s4", 2)
                        ),
                        listOf(
                                createMessage("s5", 0),
                                createMessage("s5", 1),
                                createMessage("s4", 0),
                                createMessage("s4", 1),
                                createMessage("s4", 2)
                        )),
                arrayOf(
                        listOf(
                                createMessage("s6", 0),
                                createMessage("s6", 2),
                                createMessage("s6", 3)
                        ),
                        listOf(
                                createMessage("s6", 0),
                                createMessage("s6", 1),
                                createMessage("s6", 2),
                                createMessage("s6", 3)
                        )),

                // Skip duplicates
                arrayOf(
                        listOf(
                                createMessage("s0", 0),
                                createMessage("s0", 0),
                                createMessage("s0", 1),
                                createMessage("s0", 0),
                                createMessage("s0", 2),
                                createMessage("s0", 2)
                        ),
                        listOf(
                                createMessage("s0", 0),
                                createMessage("s0", 1),
                                createMessage("s0", 2)
                        )),

                arrayOf(
                        listOf(
                                createMessage("s0", 0),
                                createMessage("s0", 1),
                                createMessage("s0", 2),
                                createMessage("s0", 0),
                                createMessage("s0", 1),
                                createMessage("s0", 2)),

                        listOf(
                                createMessage("s0", 0),
                                createMessage("s0", 1),
                                createMessage("s0", 2)
                        )),

                // reordering
                arrayOf(
                        listOf(
                                createMessage("s7", 4),
                                createMessage("s7", 0),
                                createMessage("s7", 3)
                        ),
                        listOf(
                                createMessage("s7", 0),
                                createMessage("s7", 1),
                                createMessage("s7", 2),
                                createMessage("s7", 3),
                                createMessage("s7", 4)
                        )),

                // Suppresing invalid TeamCity messages
                arrayOf(listOf(createMessage("s1", 0) + "##teamcity[message dsdsdsd"), listOf(createMessage("s1", 0))),
                arrayOf(listOf("##teamcity[message]"), emptyList()),
                arrayOf(listOf("##teamcity[message dsdsdsd"), emptyList()),
                arrayOf(listOf("   ##teamcity[]  "), listOf("   ")),
                arrayOf(listOf("abc##teamcity[message]"), listOf("abc")),
                arrayOf(listOf("Abc  ##teamcity[message]"), listOf("Abc  ")),

                // https://youtrack.jetbrains.com/issue/TW-73979
                arrayOf(listOf("   " + createMessage("s1", 0)), listOf("   ", createMessage("s1", 0))),
                arrayOf(listOf("abc" + createMessage("s1", 0)), listOf("abc", createMessage("s1", 0))),
                arrayOf(listOf("abc  " + createMessage("s1", 0)), listOf("abc  ", createMessage("s1", 0))),
                arrayOf(
                        listOf(
                                "##teamcity[testStarted name='Test1'##teamcity[testStarted name='Test2' source='c706bb34' index='0']",
                                "0' source='d9f9309b' index='0' flowId='598896723715600'",
                                "##teamcity[testFinished name='Test1' source='d9f9309b' index='1']",
                                "##teamcity[testFinished name='Test2' source='c706bb34' index='1']"
                        ),
                        listOf(
                                "0' source='d9f9309b' index='0' flowId='598896723715600'",
                                "##teamcity[testStarted name='Test1' source='d9f9309b' index='0']",
                                "##teamcity[testFinished name='Test1' source='d9f9309b' index='1']",
                                "##teamcity[testStarted name='Test2' source='c706bb34' index='0']",
                                "##teamcity[testFinished name='Test2' source='c706bb34' index='1']"
                        )),
        )
    }

    @Test(dataProvider = "testAddMisingMessages")
    fun shouldAddMisingMessages(output: List<String>, expectedOutout: List<String>) {
        // Given
        val guard = createInstance()
        val actualOutput = mutableListOf<String>()

        every { _messagesSource.read("s1", 1L, 1L) } returns sequenceOf(
                createMessage("s1", 1)
        )

        every { _messagesSource.read("s2", 1L, 2L) } returns sequenceOf(
                createMessage("s2", 1),
                createMessage("s2", 2)
        )

        every { _messagesSource.read("s3", 0L, 2L) } returns sequenceOf(
                createMessage("s3", 0),
                createMessage("s3", 1)
        )

        every { _messagesSource.read("s4", 0L, 2L) } returns sequenceOf(
                createMessage("s4", 0),
                createMessage("s4", 1)
        )

        every { _messagesSource.read("s6", 1L, 1L) } returns sequenceOf(
                createMessage("s6", 1)
        )

        every { _messagesSource.read("s7", 0L, 4L) } returns sequenceOf(
                createMessage("s7", 0),
                createMessage("s7", 1),
                createMessage("s7", 2),
                createMessage("s7", 3)
        )

        every { _messagesSource.read("d9f9309b", 0L, 1L) } returns sequenceOf(
                "##teamcity[testStarted name='Test1' source='d9f9309b' index='0']"
        )

        every { _messagesSource.read("c706bb34", 0L, 1L) } returns sequenceOf(
                "##teamcity[testStarted name='Test2' source='c706bb34' index='0']"
        )

        // When
        for (text in output)
        {
            actualOutput.addAll(guard.replace(text))
        }

        // Then
        Assert.assertEquals(actualOutput, expectedOutout)
    }

    private fun createInstance() =
            MessagesGuard(_messagesSource)

    private fun createMessage(source: String?, index: Int?): String {
        var message = "##teamcity[message text='a'";
        if (source != null)
        {
            message += " ${SourceAttribute}='$source'"
        }

        if (index != null)
        {
            message += " ${IndexAttribute}='$index'"
        }

        return message + "]";
    }
}
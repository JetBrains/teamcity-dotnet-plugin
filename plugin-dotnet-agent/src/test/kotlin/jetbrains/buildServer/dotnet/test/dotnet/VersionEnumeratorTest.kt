package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetSdk
import jetbrains.buildServer.dotnet.VersionEnumeratorImpl
import jetbrains.buildServer.dotnet.Versioned
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class VersionEnumeratorTest {
    @DataProvider
    fun versionsCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        emptySequence<Element>(),
                        emptyList<Pair<String, Element>>()
                ),
                arrayOf(
                        sequenceOf(
                                Element(Version(1, 2, 3), "1.2.3")
                        ),
                        listOf(
                                Pair("1.2", Element(Version(1, 2, 3), "1.2.3")),
                                Pair("1.2.3", Element(Version(1, 2, 3), "1.2.3"))
                        )
                ),
                arrayOf(
                        sequenceOf(
                                Element(Version(1, 2, 300), "1.2.300"),
                                Element(Version(1, 2, 2), "1.2.2"),
                                Element(Version(1, 2, 301), "1.2.301")
                        ),
                        listOf(
                                Pair("1.2", Element(Version(1, 2, 301), "1.2.301")),
                                Pair("1.2.300", Element(Version(1, 2, 300), "1.2.300")),
                                Pair("1.2.2", Element(Version(1, 2, 2), "1.2.2")),
                                Pair("1.2.301", Element(Version(1, 2, 301), "1.2.301"))
                        )
                ),
                arrayOf(
                        sequenceOf(
                                Element(Version(2, 2, 401), "2.2.401"),
                                Element(Version(1, 2, 300), "1.2.300"),
                                Element(Version(1, 2, 2), "1.2.2"),
                                Element(Version(2, 2, 2), "2.2.2"),
                                Element(Version(1, 2, 301), "1.2.301")
                        ),
                        listOf(
                                Pair("2.2", Element(Version(2, 2, 401), "2.2.401")),
                                Pair("2.2.401", Element(Version(2, 2, 401), "2.2.401")),
                                Pair("2.2.2", Element(Version(2, 2, 2), "2.2.2")),
                                Pair("1.2", Element(Version(1, 2, 301), "1.2.301")),
                                Pair("1.2.300", Element(Version(1, 2, 300), "1.2.300")),
                                Pair("1.2.2", Element(Version(1, 2, 2), "1.2.2")),
                                Pair("1.2.301", Element(Version(1, 2, 301), "1.2.301"))
                        )
                ),
                arrayOf(
                        sequenceOf(
                                Element(Version(3, 5), "3.5"),
                                Element(Version(2, 2, 401), "2.2.401"),
                                Element(Version(1, 2, 300), "1.2.300"),
                                Element(Version(1, 2, 2), "1.2.2"),
                                Element(Version(2, 2, 2), "2.2.2"),
                                Element(Version(1, 2, 301), "1.2.301")
                        ),
                        listOf(
                                Pair("3.5", Element(Version(3, 5), "3.5")),
                                Pair("3.5.0", Element(Version(3, 5), "3.5")),
                                Pair("2.2", Element(Version(2, 2, 401), "2.2.401")),
                                Pair("2.2.401", Element(Version(2, 2, 401), "2.2.401")),
                                Pair("2.2.2", Element(Version(2, 2, 2), "2.2.2")),
                                Pair("1.2", Element(Version(1, 2, 301), "1.2.301")),
                                Pair("1.2.300", Element(Version(1, 2, 300), "1.2.300")),
                                Pair("1.2.2", Element(Version(1, 2, 2), "1.2.2")),
                                Pair("1.2.301", Element(Version(1, 2, 301), "1.2.301"))
                        )
                )
        )
    }

    @Test(dataProvider = "versionsCases")
    fun shouldEnumerateVersions(elements: Sequence<Element>, expected: Collection<Pair<String, Element>>) {
        // Given
        val enumerator = VersionEnumeratorImpl()

        // When
        var actual = enumerator.enumerate<Element>(elements).toList()

        // Then
        Assert.assertEquals(actual, expected)
    }

    data class Element(override val version: Version, val description: String) : Versioned
}
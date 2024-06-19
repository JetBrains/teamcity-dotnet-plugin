package jetbrains.buildServer.dotnet.test.nunit.testReordering

import jetbrains.buildServer.nunit.testReordering.NUnitReorderingTestsSplitService
import jetbrains.buildServer.nunit.testReordering.TestInfo
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NUnitReorderingTestsSplitServiceTest {
    data class TestCase(
        val allTests: List<TestInfo>,
        val failedTests: List<TestInfo>,
        val expectedFirstStepTests: List<TestInfo>,
        val expectedSecondStepTests: List<TestInfo>
    )

    @DataProvider(name = "splitTestsCases")
    fun getSplitTestsCases(): Array<TestCase> = arrayOf(
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do")
            ),
            failedTests = listOf(TestInfo("class_b")),
            expectedFirstStepTests = listOf(TestInfo(File("bb.dll"), "class_b", "class_b.Do")),
            expectedSecondStepTests = listOf(TestInfo(File("aa.dll"), "class_a", "class_a.Do")),
        ),
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do")
            ),
            failedTests = listOf(),
            expectedFirstStepTests = listOf(),
            expectedSecondStepTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do")
            ),
        ),
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do")
            ),
            failedTests = listOf(TestInfo("class_b"), TestInfo("class_a")),
            expectedFirstStepTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do")
            ),
            expectedSecondStepTests = listOf(),
        ),
        TestCase(
            allTests = listOf(),
            failedTests = listOf(),
            expectedFirstStepTests = listOf(),
            expectedSecondStepTests = listOf(),
        ),
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(
                    File("bb.dll"), "class_b", "class_b.Do2"
                )
            ),
            failedTests = listOf(TestInfo("class_b")),
            expectedFirstStepTests = listOf(
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do2")
            ),
            expectedSecondStepTests = listOf(TestInfo(File("aa.dll"), "class_a", "class_a.Do")),
        ),
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(
                    File("bb.dll"), "class_b", "class_b.Do2"
                )
            ),
            failedTests = listOf(TestInfo(File("bb.dll"), "class_b", null)),
            expectedFirstStepTests = listOf(
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do2")
            ),
            expectedSecondStepTests = listOf(TestInfo(File("aa.dll"), "class_a", "class_a.Do")),
        ),
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(
                    File("bb.dll"), "class_b", "class_b.Do2"
                )
            ),
            failedTests = listOf(TestInfo(File("zzz.dll"), "class_b", null)),
            expectedFirstStepTests = listOf(),
            expectedSecondStepTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(
                    File("bb.dll"), "class_b", "class_b.Do2"
                )
            ),
        ),
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(
                    File("bb.dll"), "class_b", "class_b.Do2"
                )
            ),
            failedTests = listOf(TestInfo(File(File("bb.dll").absolutePath), "class_b", null)),
            expectedFirstStepTests = listOf(
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do2")
            ),
            expectedSecondStepTests = listOf(TestInfo(File("aa.dll"), "class_a", "class_a.Do")),
        ),
        TestCase(
            allTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do"),
                TestInfo(
                    File("zzz.dll"), "class_b", "class_b.Do2"
                )
            ),
            failedTests = listOf(TestInfo(File("zzz.dll"), "class_b", null)),
            expectedFirstStepTests = listOf(TestInfo(File("zzz.dll"), "class_b", "class_b.Do2")),
            expectedSecondStepTests = listOf(
                TestInfo(File("aa.dll"), "class_a", "class_a.Do"),
                TestInfo(File("bb.dll"), "class_b", "class_b.Do")
            ),
        ),
    )

    @Test(dataProvider = "splitTestsCases")
    fun `should split test cases`(testCase: TestCase) {
        // arrange
        val service = NUnitReorderingTestsSplitService()

        // act
        val split = service.splitTests(testCase.allTests, testCase.failedTests)

        // assert
        Assert.assertEquals(split.firstStepTests, testCase.expectedFirstStepTests)
        Assert.assertEquals(split.secondStepTests, testCase.expectedSecondStepTests)
    }
}
package jetbrains.buildServer.dotnet.test.integration

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.FileSystemServiceImpl
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryFilterProviderImpl
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryReportReader
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetrySettings
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import org.testcontainers.containers.Container
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory

class TestRetryFilterIntegrationTest {
    enum class TestFramework { MsTest, NUnit, XUnit }

    private var containers: List<DotnetSdkContainer> = listOf(
        DotnetSdkContainer("mcr.microsoft.com/dotnet/sdk:8.0"),
        DotnetSdkContainer("mcr.microsoft.com/dotnet/sdk:9.0"),
        DotnetSdkContainer("mcr.microsoft.com/dotnet/sdk:latest"),
    )

    private val retrySettings = mockk<TestRetrySettings>()
    private val retryReportReader = TestRetryReportReader(retrySettings, FileSystemServiceImpl())
    private val retryFilterProvider = TestRetryFilterProviderImpl(mockk(relaxed = true))

    @BeforeClass(alwaysRun = true)
    fun setUp() = containers.forEach {
        it.start()
        it.generateTestProjectFile()
    }

    @AfterClass(alwaysRun = true)
    fun tearDown() = containers.forEach {
        it.stop()
    }

    data class TestRetryFilterCase(val sourceCode: String, val framework: TestFramework, val expectedFilter: String)

    @DataProvider
    fun testRetryFilterTestData(): Iterator<TestRetryFilterCase> = listOf(
        // NUnit
        TestRetryFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            public class SimpleTestClass
            {
                [Test] public void SimpleTestMethod() => Assert.Fail();
            }
        """.trimIndent(),
            framework = TestFramework.NUnit,
            expectedFilter = "FullyQualifiedName=IntegrationTests.NUnit.SimpleTestClass.SimpleTestMethod"
        ),
        TestRetryFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            public class SimpleTestClass
            {
                [TestCase(42)] public void TestCaseTestMethod(int param) => Assert.Fail();
            }
        """.trimIndent(),
            framework = TestFramework.NUnit,
            expectedFilter = "FullyQualifiedName~IntegrationTests.NUnit.SimpleTestClass.TestCaseTestMethod"
        ),
        TestRetryFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            [TestFixture(42)] public class TestFixtureClass(int param)
            {
                [Test] public void SimpleTestMethod() => Assert.Fail();
            }
        """.trimIndent(),
            framework = TestFramework.NUnit,
            expectedFilter = "FullyQualifiedName~IntegrationTests.NUnit.TestFixtureClass"
        ),
        TestRetryFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            public class SimpleTestClass
            {
                [Test] public void RandomParameterTestMethod([Random(min: -1.0, max: 1.0, count: 1)] double _) => Assert.Fail();
            }
        """.trimIndent(),
            framework = TestFramework.NUnit,
            expectedFilter = "FullyQualifiedName~IntegrationTests.NUnit.SimpleTestClass.RandomParameterTestMethod"
        ),
        TestRetryFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            public class SimpleTestClass
            {
                [Test] public void GenericTestMethod<T>(T t1) => Assert.Fail();
            }
        """.trimIndent(),
            framework = TestFramework.NUnit,
            expectedFilter = "FullyQualifiedName~IntegrationTests.NUnit.SimpleTestClass.GenericTestMethod"
        ),
        TestRetryFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            [TestFixture(42)] public class GenericTestClass<T1>(T1 left)
            {
                [TestCase(43)] public void GenericTestMethod<T2>(T2 right) => Assert.That(left, Is.EqualTo(right));
            }
        """.trimIndent(),
            framework = TestFramework.NUnit,
            expectedFilter = "FullyQualifiedName~IntegrationTests.NUnit.GenericTestClass"
        ),
        // XUnit
        TestRetryFilterCase(
            """
                using Xunit;
                namespace IntegrationTests.XUnit;
                public class SimpleTestClass
                {
                    [Fact] public void SimpleTestMethod() => Assert.Fail();
                }
        """.trimIndent(),
            framework = TestFramework.XUnit,
            expectedFilter = "FullyQualifiedName=IntegrationTests.XUnit.SimpleTestClass.SimpleTestMethod"
        ),
        TestRetryFilterCase(
            """
                using Xunit;
                namespace IntegrationTests.XUnit;
                public class TheoryTestClass
                {
                    [Theory]
                    [InlineData(42)]
                    public void InlineDataTestMethod(int value) => Assert.Fail();
                }
        """.trimIndent(),
            framework = TestFramework.XUnit,
            expectedFilter = "FullyQualifiedName=IntegrationTests.XUnit.TheoryTestClass.InlineDataTestMethod"
        ),
        // MSTest
        TestRetryFilterCase(
            """
                using Microsoft.VisualStudio.TestTools.UnitTesting;
                namespace IntegrationTests.MSTest;
                [TestClass] public class SimpleTestClass
                {
                    [TestMethod] public void SimpleTestMethod() => Assert.Fail();
                }
        """.trimIndent(),
            framework = TestFramework.MsTest,
            expectedFilter = "FullyQualifiedName=IntegrationTests.MSTest.SimpleTestClass.SimpleTestMethod"
        ),
        TestRetryFilterCase(
            """
                using Microsoft.VisualStudio.TestTools.UnitTesting;
                namespace IntegrationTests.MSTest;
                [TestClass] public class SimpleTestClass
                {
                    [TestMethod] [DataRow(42)] public void DataRowTestMethod(int x) => Assert.Fail();
                }
        """.trimIndent(),
            framework = TestFramework.MsTest,
            expectedFilter = "FullyQualifiedName=IntegrationTests.MSTest.SimpleTestClass.DataRowTestMethod"
        ),
        TestRetryFilterCase(
            """
                using System.Collections.Generic;
                using Microsoft.VisualStudio.TestTools.UnitTesting;
                namespace IntegrationTests.MSTest;
                [TestClass] public class SimpleTestClass
                {
                    public static IEnumerable<object[]> DynamicData => new[] { new object[] { 42 } };
                    [TestMethod] [DynamicData(nameof(DynamicData))] public void DynamicDataTestMethod(int x) => Assert.Fail();
                }
        """.trimIndent(),
            framework = TestFramework.MsTest,
            expectedFilter = "FullyQualifiedName=IntegrationTests.MSTest.SimpleTestClass.DynamicDataTestMethod"
        ),
    ).iterator()

    @Test(groups = ["integration"], dataProvider = "testRetryFilterTestData")
    fun `should build correct test filter for different retry scenarios`(testCase: TestRetryFilterCase) =
        containers.forEach { container ->
            // arrange
            container.createFileInContainer("/app/Test.cs", testCase.sourceCode)

            // act, step 1: run dotnet test for the first time
            val testResult = container.runDotnetTest()
            assertEquals(testResult.exitCode, 1, "dotnet test is expected to fail: $testResult")

            val failedTests = retryReportReader.readFailedTestNames().also { retryReportReader.cleanup() }
            assertEquals(failedTests.size, 1, "one test is expected to fail: $testResult")

            // act, step 2: build test filter
            retryFilterProvider.setTestNames(failedTests)
            val filter = retryFilterProvider.getFilterExpression(TestsSplittingMode.Disabled)
            assertEquals(filter, testCase.expectedFilter)

            // act, step 3: retry dotnet test with filter
            val testRetryResult = container.runDotnetTest(filter)
            assertEquals(testRetryResult.exitCode, 1, "dotnet test is expected to fail: $testRetryResult")

            val retryFailedTests = retryReportReader.readFailedTestNames().also { retryReportReader.cleanup() }
            assertEquals(retryFailedTests.size, 1, "one test is expected to fail after retry: $testRetryResult")
        }

    data class TestFilterCase(
        val sourceCode: String,
        val framework: TestFramework,
        val filter: String,
        val expectedExitCode: Int
    )

    @DataProvider
    fun testFilterTestData(): Iterator<TestFilterCase> = listOf(
        TestFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            public class TestClass
            {
                [TestCase(42)] public void TestFail(int param) => Assert.Fail();
                [TestCase(42)] public void TestPass(int param) => Assert.Pass();
            }
        """.trimIndent(),
            TestFramework.NUnit,
            filter = "FullyQualifiedName~IntegrationTests.NUnit.TestClass.TestPass",
            expectedExitCode = 0
        ),
        TestFilterCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            public class TestClass
            {
                [TestCase(42)] public void TestFail(int param) => Assert.Fail();
                [TestCase(42)] public void TestPass(int param) => Assert.Pass();
            }
        """.trimIndent(),
            TestFramework.NUnit,
            filter = "FullyQualifiedName~IntegrationTests.NUnit.TestClass.TestFail",
            expectedExitCode = 1
        ),
        TestFilterCase(
            """
                using Xunit;
                namespace IntegrationTests.XUnit;
                public class TestClass
                {
                    [Theory] [InlineData(42)] public void TestFail(int value) => Assert.Fail();
                    [Theory] [InlineData(42)] public void TestPass(int value) { }
                }
        """.trimIndent(),
            TestFramework.XUnit,
            filter = "FullyQualifiedName=IntegrationTests.XUnit.TestClass.TestPass",
            expectedExitCode = 0
        ),
        TestFilterCase(
            """
                using Xunit;
                namespace IntegrationTests.XUnit;
                public class TestClass
                {
                    [Theory] [InlineData(42)] public void TestFail(int value) => Assert.Fail();
                    [Theory] [InlineData(42)] public void TestPass(int value) { }
                }
        """.trimIndent(),
            TestFramework.XUnit,
            filter = "FullyQualifiedName=IntegrationTests.XUnit.TestClass.TestFail",
            expectedExitCode = 1
        ),
        TestFilterCase(
            """
                using Microsoft.VisualStudio.TestTools.UnitTesting;
                namespace IntegrationTests.MSTest;
                [TestClass] public class TestClass
                {
                    [TestMethod] [DataRow(42)] public void TestFail(int x) => Assert.Fail();
                    [TestMethod] [DataRow(42)] public void TestPass(int x) { }
                }
        """.trimIndent(),
            TestFramework.MsTest,
            filter = "FullyQualifiedName=IntegrationTests.MSTest.TestClass.TestPass",
            expectedExitCode = 0
        ),
        TestFilterCase(
            """
                using Microsoft.VisualStudio.TestTools.UnitTesting;
                namespace IntegrationTests.MSTest;
                [TestClass] public class TestClass
                {
                    [TestMethod] [DataRow(42)] public void TestFail(int x) => Assert.Fail();
                    [TestMethod] [DataRow(42)] public void TestPass(int x) { }
                }
        """.trimIndent(),
            TestFramework.MsTest,
            filter = "FullyQualifiedName=IntegrationTests.MSTest.TestClass.TestFail",
            expectedExitCode = 1
        )
    ).iterator()

    @Test(groups = ["integration"], dataProvider = "testFilterTestData")
    fun `should run only filtered test cases`(testCase: TestFilterCase) {
        containers.forEach { container ->
            // arrange
            container.createFileInContainer("/app/Test.cs", testCase.sourceCode)

            // act
            val testResult = container.runDotnetTest(testCase.filter)

            // assert
            assertEquals(testResult.exitCode, testCase.expectedExitCode)
        }
    }

    private fun DotnetSdkContainer.runDotnetTest(filter: String? = null): Container.ExecResult {
        initRetryReportPath()
        val filterArgs = filter?.let { listOf("--filter", it) } ?: emptyList()
        val args = listOf(
            "dotnet",
            "test",
            "--logger",
            "teamcity",
            "-e",
            "TEAMCITY_FAILED_TESTS_REPORTING_PATH=${retrySettings.reportPath}",
            "-e",
            "TEAMCITY_VERSION=2024.12.1",
        )

        return execInContainer(*(args + filterArgs).toTypedArray())
    }

    private fun DotnetSdkContainer.initRetryReportPath() {
        val retryReportTempFolder = tempMount.resolve(java.util.UUID.randomUUID().toString())
            .createDirectory()
            .absolutePathString()

        every { retrySettings.reportPath } returns retryReportTempFolder
        every { retrySettings.maxFailures } returns 10
    }

    private fun DotnetSdkContainer.generateTestProjectFile() {
        val packages = listOf(
            "Microsoft.NET.Test.Sdk" to "17.12.0",
            "MSTest.TestAdapter" to "3.6.2",
            "MSTest.TestFramework" to "3.6.2",
            "NUnit" to "4.2.2",
            "NUnit3TestAdapter" to "4.6.0",
            "xunit" to "2.9.2",
            "xunit.runner.visualstudio" to "2.8.2",
            "TeamCity.VSTest.TestAdapter" to "1.0.42"
        )

        execOrFail(
            command = listOf("dotnet", "new", "classlib", "-n", "Tests", "-o", "."),
            errorMessage = "Failed to create project"
        )
        packages.forEach { (packageName, version) ->
            execOrFail(
                command = listOf("dotnet", "add", "package", packageName, "-v", version, "--no-restore"),
                errorMessage = "Failed to install package $packageName"
            )
        }
    }

    private fun DotnetSdkContainer.execOrFail(command: List<String>, errorMessage: String) {
        val execResult = execInContainer(*command.toTypedArray())
        if (execResult.exitCode != 0) {
            Assert.fail("$errorMessage: $execResult")
        }
    }
}
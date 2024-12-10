package jetbrains.buildServer.dotnet.test.integration

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.FileSystemServiceImpl
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryFilterProviderImpl
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryReportReader
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetrySettings
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import org.testcontainers.containers.Container
import org.testcontainers.utility.MountableFile
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory

class TestRetryFilterIntegrationTest {
    enum class TestFramework { MsTest, NUnit, XUnit }
    data class TestCase(val sourceCode: String, val framework: TestFramework, val expectedFilter: String)

    private var containers: List<DotnetSdkContainer> = listOf(
        DotnetSdkContainer("mcr.microsoft.com/dotnet/sdk:8.0"),
        DotnetSdkContainer("mcr.microsoft.com/dotnet/sdk:latest"),
    )

    private val retrySettings = mockk<TestRetrySettings>()
    private val retryReportReader = TestRetryReportReader(retrySettings, FileSystemServiceImpl())
    private val retryFilterProvider = TestRetryFilterProviderImpl()

    @BeforeClass
    fun setUp() = containers.forEach {
        it.start()
        it.generateTestProjectFile()
    }

    @AfterClass
    fun tearDown() = containers.forEach {
        it.stop()
    }

    @DataProvider
    fun testData(): Iterator<TestCase> = listOf(
        // NUnit
        TestCase(
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
        TestCase(
            """
            using NUnit.Framework;
            namespace IntegrationTests.NUnit;
            public class SimpleTestClass
            {
                [TestCase(42)] public void TestCaseTestMethod() => Assert.Fail();
            }
        """.trimIndent(),
            framework = TestFramework.NUnit,
            expectedFilter = "FullyQualifiedName~IntegrationTests.NUnit.SimpleTestClass.TestCaseTestMethod"
        ),
        TestCase(
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
        TestCase(
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
        TestCase(
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
        TestCase(
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
        TestCase(
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
        TestCase(
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
        TestCase(
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
        TestCase(
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
        TestCase(
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

    @Test(dataProvider = "testData")
    fun `should build correct test filter for different retry scenarios`(testCase: TestCase) =
        containers.forEach { container ->
            // arrange
            container.createFileInContainer("/app/Test.cs", testCase.sourceCode)
            container.copyTestAdapter() // temp solution

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

    private fun DotnetSdkContainer.copyTestAdapter() {
        val fs = FileSystemServiceImpl()
        val adapterPath = File("src/test/kotlin/jetbrains/buildServer/dotnet/test/integration/test-adapter")
        for (file in fs.list(adapterPath)) {
            copyFileToContainer(
                MountableFile.forHostPath(file.absolutePath), "/app/test-adapter/" + file.name
            )
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
            "--test-adapter-path",
            "/app/test-adapter/",
            "/p:TEAMCITY_VERSION=2024.11",
            "-e",
            "TEAMCITY_FAILED_TESTS_REPORTING_PATH=${retrySettings.reportPath}"
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
            "xunit.runner.visualstudio" to "2.8.2"
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
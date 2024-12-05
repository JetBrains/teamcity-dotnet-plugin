package jetbrains.buildServer.dotnet.test.integration

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.FileSystemServiceImpl
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryFilterProviderImpl
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryReportReader
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetrySettings
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.Container
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.MountableFile
import org.testng.Assert.assertEquals
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory

class DotnetSdkContainer : GenericContainer<DotnetSdkContainer>("mcr.microsoft.com/dotnet/sdk:9.0")

class TestRetryFilterIntegrationTest {
    enum class TestFramework { MsTest, NUnit, XUnit }
    data class TestCase(val sourceCode: String, val framework: TestFramework, val expectedFilter: String)

    private val retrySettings = mockk<TestRetrySettings>()
    private val retryReportReader = TestRetryReportReader(retrySettings, FileSystemServiceImpl())

    private lateinit var container: DotnetSdkContainer
    private lateinit var containerMount: Path
    private val filterProvider = TestRetryFilterProviderImpl()

    @BeforeClass
    fun setUp() {
        containerMount = createTempDirectory()
        container = DotnetSdkContainer()
            .withWorkingDirectory("/app")
            .withEnv(mapOf("TEAMCITY_VERSION" to "2024.11"))
            // container volume to get test retry reports
            .withFileSystemBind(
                containerMount.absolutePathString(),
                containerMount.absolutePathString(),
                BindMode.READ_WRITE
            )
            // keep the container running for the test
            .withCommand("tail", "-f", "/dev/null")
            .also { it.start() }
    }

    @AfterClass
    fun tearDown() = container.stop()

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
    fun `should build correct test filter for different retry scenarios`(testCase: TestCase) {
        // arrange
        createTestProjectInContainer(testCase)
        copyTestAdapter() // temp solution

        // act, step 1: run dotnet test for the first time
        val testResult = runDotnetTest()
        assertEquals(testResult.exitCode, 1, "dotnet test is expected to fail: ${testResult.stdout}")

        val failedTests = retryReportReader.readFailedTestNames().also { retryReportReader.cleanup() }
        assertEquals(failedTests.size, 1, "one test is expected to fail: ${testResult.stdout}")

        // act, step 2: build test filter
        filterProvider.setTestNames(failedTests)
        val filter = filterProvider.getFilterExpression(TestsSplittingMode.Disabled)
        assertEquals(filter, testCase.expectedFilter)

        // act, step 3: retry dotnet test with filter
        val testRetryResult = runDotnetTest(filter)
        assertEquals(testRetryResult.exitCode, 1, "dotnet test is expected to fail: ${testRetryResult.stdout}")

        val retryFailedTests = retryReportReader.readFailedTestNames().also { retryReportReader.cleanup() }
        assertEquals(retryFailedTests.size, 1, "one test is expected to fail after retry: ${testRetryResult.stdout}")
        assertEquals(retryFailedTests[0], failedTests[0], "the same test should fail after retry")
    }

    private fun createTestProjectInContainer(testCase: TestCase) {
        val projectFile = generateProjectFile(testCase.framework)
        createFileInContainer("Tests", ".csproj", projectFile, "/app/Tests.csproj")
        createFileInContainer("Test", ".cs", testCase.sourceCode, "/app/Test.cs")
    }

    private fun createFileInContainer(prefix: String, suffix: String, content: String, containerPath: String) {
        val file = File.createTempFile(prefix, suffix)
        try {
            file.writeText(content)
            container.copyFileToContainer(MountableFile.forHostPath(file.absolutePath), containerPath)
        } finally {
            file.delete()
        }
    }

    private fun copyTestAdapter() {
        val fs = FileSystemServiceImpl()
        val adapterPath = File("src/test/kotlin/jetbrains/buildServer/dotnet/test/integration/test-adapter")
        for (file in fs.list(adapterPath)) {
            container.copyFileToContainer(
                MountableFile.forHostPath(file.absolutePath), "/app/test-adapter/" + file.name
            )
        }
    }

    private fun runDotnetTest(filter: String? = null): Container.ExecResult {
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

        return container.execInContainer(*(args + filterArgs).toTypedArray())
    }

    private fun initRetryReportPath() {
        val retryReportTempFolder = containerMount.resolve(java.util.UUID.randomUUID().toString())
            .createDirectory()
            .absolutePathString()

        every { retrySettings.reportPath } returns retryReportTempFolder
        every { retrySettings.maxFailures } returns 10
    }

    private fun generateProjectFile(framework: TestFramework) = buildString {
        append(
            """
            <Project Sdk="Microsoft.NET.Sdk">
                <PropertyGroup>
                    <TargetFramework>net9.0</TargetFramework>
                    <IsTestProject>true</IsTestProject>
                </PropertyGroup>
                <ItemGroup>
                    <PackageReference Include="Microsoft.NET.Test.Sdk" Version="17.12.0"/>
        """.trimIndent()
        )

        append(
            when (framework) {
                TestFramework.MsTest -> """
                        <PackageReference Include="MSTest.TestAdapter" Version="3.6.2"/>
                        <PackageReference Include="MSTest.TestFramework" Version="3.6.2"/>
                    </ItemGroup>
                </Project>
                """.trimIndent()

                TestFramework.NUnit -> """
                        <PackageReference Include="NUnit" Version="4.2.2"/>
                        <PackageReference Include="NUnit3TestAdapter" Version="4.6.0"/>
                    </ItemGroup>
                </Project>
                """.trimIndent()

                TestFramework.XUnit -> """
                        <PackageReference Include="xunit" Version="2.9.2"/>
                        <PackageReference Include="xunit.runner.visualstudio" Version="2.8.2">
                            <IncludeAssets>runtime; build; native; contentfiles; analyzers; buildtransitive</IncludeAssets>
                            <PrivateAssets>all</PrivateAssets>
                        </PackageReference>
                    </ItemGroup>
                </Project>
                """.trimIndent()
            }
        )
    }
}
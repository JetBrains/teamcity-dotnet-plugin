package jetbrains.buildServer.dotnet.test.nunit.testReordering

import jetbrains.buildServer.XmlDocumentServiceImpl
import jetbrains.buildServer.nunit.testReordering.NUnitXmlTestInfoParser
import jetbrains.buildServer.nunit.testReordering.TestInfo
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class NUnitXmlTestInfoParserTest {
    data class TestCase(val expectedTests: List<TestInfo>, val xmlDocument: String)

    @DataProvider(name = "testCases")
    fun getCases(): Array<TestCase> = arrayOf(
        TestCase(
            listOf(
                TestInfo(
                    assembly = File("""C:\Projects\trunk\HelloWorld.Tests\bin\Debug\HelloWorld.Tests.dll"""),
                    className = "HelloWorld.Tests.UnitTest1",
                    fullMethodName = "HelloWorld.Tests.UnitTest1.TestMethodCatA"
                ),
                TestInfo(
                    assembly = File("""C:\Projects\trunk\HelloWorld.Tests\bin\Debug\HelloWorld.Tests.dll"""),
                    className = "HelloWorld.Tests.UnitTest1",
                    fullMethodName = "HelloWorld.Tests.UnitTest4.TestMethodCatM08"
                ),
                TestInfo(
                    assembly = File("""C:\Projects\trunk\HellowWorld.Tests2\bin\Debug\HelloWorld.Tests2.dll"""),
                    className = "HelloWorld.Tests2.UnitTest1",
                    fullMethodName = "HelloWorld.Tests2.UnitTest1.TestDo"
                )
            ),
            xmlSample
        ),
        TestCase(
            listOf(
                TestInfo(
                    assembly = File("""C:\Projects\trunk\HelloWorld.Tests\bin\Debug\HelloWorld.Tests.dll"""),
                    className = "HelloWorld.Tests.UnitTest1",
                    fullMethodName = "HelloWorld.Tests.UnitTest1.TestMethodCatA"
                ),
                TestInfo(
                    assembly = File("""C:\Projects\trunk\HelloWorld.Tests\bin\Debug\HelloWorld.Tests.dll"""),
                    className = "HelloWorld.Tests.UnitTest1",
                    fullMethodName = "HelloWorld.Tests.UnitTest4.TestMethodCatM08"
                ),
                TestInfo(
                    assembly = File("""C:\Projects\trunk\HellowWorld.Tests2\bin\Debug\HelloWorld.Tests2.dll"""),
                    className = "HelloWorld.Tests2.UnitTest1",
                    fullMethodName = "HelloWorld.Tests2.UnitTest1.TestDo"
                )
            ),
            xmlWithoutDeclarationSample
        ),
        TestCase(
            listOf(
                TestInfo(
                    assembly = File("""C:\Projects\testData\NUnit\category.dll"""),
                    className = "TeamCity.NUnit.Category.STest",
                    fullMethodName = "TeamCity.NUnit.Category.STest.C_Test103_c1"
                ),
                TestInfo(
                    assembly = File("""C:\Projects\testData\NUnit\category.dll"""),
                    className = "TeamCity.NUnit.Category.STest",
                    fullMethodName = "TeamCity.NUnit.Category.STest.C_Test103_c2"
                ),
                TestInfo(
                    assembly = File("""C:\Projects\testData\NUnit\category.dll"""),
                    className = "TeamCity.NUnit.Category.STest",
                    fullMethodName = "TeamCity.NUnit.Category.STest.C_Test103_c3"
                )
            ),
            xmlWithoutClassNameAttrSample
        ),
    )

    @Test(dataProvider = "testCases")
    fun `should parse tests from nunit xml`(testCase: TestCase) {
        // arrange
        val parser = NUnitXmlTestInfoParser(XmlDocumentServiceImpl())

        // act
        val tests = parser.parse(testCase.xmlDocument)

        // assert
        Assert.assertEquals(tests, testCase.expectedTests)
    }

    private val xmlSample = """
        <?xml version="1.0" encoding="utf-8" standalone="no"?>
        <test-run id="2" testcasecount="35">
          <test-suite type="Project" id="2" name="LQr4qNv9qVpUq0xKLrqMDiQtAXmPfVwj.nunit" fullname="C:\Projects\TeamServer\BuildServer\.idea_artifacts\agent_deployment_debug\temp\buildTmp\LQr4qNv9qVpUq0xKLrqMDiQtAXmPfVwj.nunit" testcasecount="35">
            <test-suite type="Assembly" id="3-1038" name="HelloWorld.Tests.dll" fullname="C:\Projects\trunk\HelloWorld.Tests\bin\Debug\HelloWorld.Tests.dll" runstate="Runnable" testcasecount="34">
              <properties>
                <property name="ParallelScope" value="Self" />
                <property name="_PID" value="20208" />
                <property name="_APPDOMAIN" value="test-domain-HelloWorld.Tests.dll" />
              </properties>
              <test-suite type="TestSuite" id="3-1039" name="HelloWorld" fullname="HelloWorld" runstate="Runnable" testcasecount="34">
                <test-suite type="TestSuite" id="3-1040" name="Tests" fullname="HelloWorld.Tests" runstate="Runnable" testcasecount="34">
                  <test-suite type="TestFixture" id="3-1018" name="UnitTest1" fullname="HelloWorld.Tests.UnitTest1" classname="HelloWorld.Tests.UnitTest1" runstate="Runnable" testcasecount="17">
                    <properties>
                      <property name="ParallelScope" value="Self" />
                    </properties>
                    <test-case id="3-1024" name="TestMethodCatA" fullname="HelloWorld.Tests.UnitTest1.TestMethodCatA" methodname="TestMethodCatA" classname="HelloWorld.Tests.UnitTest1" runstate="Runnable" seed="674926834">
                      <properties>
                        <property name="ParallelScope" value="Self" />
                        <property name="Category" value="A" />
                      </properties>
                    </test-case>
                    <test-case id="3-1012" name="TestMethodCatM08" fullname="HelloWorld.Tests.UnitTest4.TestMethodCatM08" methodname="TestMethodCatM08" classname="HelloWorld.Tests.UnitTest1" runstate="Runnable" seed="1838194091">
                      <properties>
                        <property name="ParallelScope" value="Self" />
                        <property name="Category" value="M" />
                      </properties>
                    </test-case>
                  </test-suite>
                </test-suite>
              </test-suite>
            </test-suite>
            <test-suite type="Assembly" id="4-1002" name="HelloWorld.Tests2.dll" fullname="C:\Projects\trunk\HellowWorld.Tests2\bin\Debug\HelloWorld.Tests2.dll" runstate="Runnable" testcasecount="1">
              <properties>
                <property name="_PID" value="10504" />
                <property name="_APPDOMAIN" value="test-domain-HelloWorld.Tests2.dll" />
              </properties>
              <test-suite type="TestSuite" id="4-1003" name="HelloWorld" fullname="HelloWorld" runstate="Runnable" testcasecount="1">
                <test-suite type="TestSuite" id="4-1004" name="Tests2" fullname="HelloWorld.Tests2" runstate="Runnable" testcasecount="1">
                  <test-suite type="TestFixture" id="4-1000" name="UnitTest1" fullname="HelloWorld.Tests2.UnitTest1" classname="HelloWorld.Tests2.UnitTest1" runstate="Runnable" testcasecount="1">
                    <properties>
                      <property name="ParallelScope" value="Self" />
                    </properties>
                    <test-case id="4-1001" name="TestDo" fullname="HelloWorld.Tests2.UnitTest1.TestDo" methodname="TestDo" classname="HelloWorld.Tests2.UnitTest1" runstate="Runnable" seed="1998102471">
                      <properties>
                        <property name="ParallelScope" value="Self" />
                      </properties>
                    </test-case>
                  </test-suite>
                </test-suite>
              </test-suite>
            </test-suite>
          </test-suite>
        </test-run>""".trimIndent()

    private val xmlWithoutDeclarationSample = """
        <test-run id="2" testcasecount="35">
            <test-suite type="Project" id="2" name="LQr4qNv9qVpUq0xKLrqMDiQtAXmPfVwj.nunit" fullname="C:\Projects\TeamServer\BuildServer\.idea_artifacts\agent_deployment_debug\temp\buildTmp\LQr4qNv9qVpUq0xKLrqMDiQtAXmPfVwj.nunit" testcasecount="35">
            <test-suite type="Assembly" id="3-1038" name="HelloWorld.Tests.dll" fullname="C:\Projects\trunk\HelloWorld.Tests\bin\Debug\HelloWorld.Tests.dll" runstate="Runnable" testcasecount="34">
              <properties>
                <property name="ParallelScope" value="Self" />
                <property name="_PID" value="20208" />
                <property name="_APPDOMAIN" value="test-domain-HelloWorld.Tests.dll" />
              </properties>
              <test-suite type="TestSuite" id="3-1039" name="HelloWorld" fullname="HelloWorld" runstate="Runnable" testcasecount="34">
                <test-suite type="TestSuite" id="3-1040" name="Tests" fullname="HelloWorld.Tests" runstate="Runnable" testcasecount="34">
                  <test-suite type="TestFixture" id="3-1018" name="UnitTest1" fullname="HelloWorld.Tests.UnitTest1" classname="HelloWorld.Tests.UnitTest1" runstate="Runnable" testcasecount="17">
                    <properties>
                      <property name="ParallelScope" value="Self" />
                    </properties>
                    <test-case id="3-1024" name="TestMethodCatA" fullname="HelloWorld.Tests.UnitTest1.TestMethodCatA" methodname="TestMethodCatA" classname="HelloWorld.Tests.UnitTest1" runstate="Runnable" seed="674926834">
                      <properties>
                        <property name="ParallelScope" value="Self" />
                        <property name="Category" value="A" />
                      </properties>
                    </test-case>
                    <test-case id="3-1012" name="TestMethodCatM08" fullname="HelloWorld.Tests.UnitTest4.TestMethodCatM08" methodname="TestMethodCatM08" classname="HelloWorld.Tests.UnitTest1" runstate="Runnable" seed="1838194091">
                      <properties>
                        <property name="ParallelScope" value="Self" />
                        <property name="Category" value="M" />
                      </properties>
                    </test-case>
                  </test-suite>
                </test-suite>
              </test-suite>
            </test-suite>
            <test-suite type="Assembly" id="4-1002" name="HelloWorld.Tests2.dll" fullname="C:\Projects\trunk\HellowWorld.Tests2\bin\Debug\HelloWorld.Tests2.dll" runstate="Runnable" testcasecount="1">
              <properties>
                <property name="_PID" value="10504" />
                <property name="_APPDOMAIN" value="test-domain-HelloWorld.Tests2.dll" />
              </properties>
              <test-suite type="TestSuite" id="4-1003" name="HelloWorld" fullname="HelloWorld" runstate="Runnable" testcasecount="1">
                <test-suite type="TestSuite" id="4-1004" name="Tests2" fullname="HelloWorld.Tests2" runstate="Runnable" testcasecount="1">
                  <test-suite type="TestFixture" id="4-1000" name="UnitTest1" fullname="HelloWorld.Tests2.UnitTest1" classname="HelloWorld.Tests2.UnitTest1" runstate="Runnable" testcasecount="1">
                    <properties>
                      <property name="ParallelScope" value="Self" />
                    </properties>
                    <test-case id="4-1001" name="TestDo" fullname="HelloWorld.Tests2.UnitTest1.TestDo" methodname="TestDo" classname="HelloWorld.Tests2.UnitTest1" runstate="Runnable" seed="1998102471">
                      <properties>
                        <property name="ParallelScope" value="Self" />
                      </properties>
                    </test-case>
                  </test-suite>
                </test-suite>
              </test-suite>
            </test-suite>
            </test-suite>
        </test-run>"""

    private val xmlWithoutClassNameAttrSample = """
        ﻿<?xml version="1.0" encoding="utf-8" standalone="no"?>
        <test-run id="2" testcasecount="3">
          <test-suite type="Project" id="2" name="aH49NFZ7bDcWy9C436DkB2zrlwrvFqZ7.nunit" fullname="C:\Temp\test1842710970\buildTmp\aH49NFZ7bDcWy9C436DkB2zrlwrvFqZ7.nunit" testcasecount="3">
            <test-suite type="Assembly" id="1-1004" name="C:\Projects\testData\NUnit\category.dll" fullname="C:\Projects\testData\NUnit\category.dll" runstate="Runnable" testcasecount="3">
              <properties>
                <property name="_PID" value="1472" />
                <property name="_APPDOMAIN" value="test-domain-aH49NFZ7bDcWy9C436DkB2zrlwrvFqZ7.nunit" />
              </properties>
              <test-suite type="Namespace" id="1-1005" name="TeamCity" fullname="TeamCity" runstate="Runnable" testcasecount="3">
                <test-suite type="Namespace" id="1-1006" name="NUnit" fullname="TeamCity.NUnit" runstate="Runnable" testcasecount="3">
                  <test-suite type="Namespace" id="1-1007" name="Category" fullname="TeamCity.NUnit.Category" runstate="Runnable" testcasecount="3">
                    <test-suite type="TestFixture" id="1-1000" name="STest" fullname="TeamCity.NUnit.Category.STest" runstate="Runnable" testcasecount="3">
                      <test-case id="1-1001" name="C_Test103_c1" fullname="TeamCity.NUnit.Category.STest.C_Test103_c1" runstate="Runnable">
                        <properties>
                          <property name="_CATEGORIES" value="C1" />
                        </properties>
                      </test-case>
                      <test-case id="1-1002" name="C_Test103_c2" fullname="TeamCity.NUnit.Category.STest.C_Test103_c2" runstate="Runnable">
                        <properties>
                          <property name="_CATEGORIES" value="C2" />
                        </properties>
                      </test-case>
                      <test-case id="1-1003" name="C_Test103_c3" fullname="TeamCity.NUnit.Category.STest.C_Test103_c3" runstate="Runnable">
                        <properties>
                          <property name="_CATEGORIES" value="C3" />
                        </properties>
                      </test-case>
                    </test-suite>
                  </test-suite>
                </test-suite>
              </test-suite>
            </test-suite>
          </test-suite>
        </test-run>""".trimIndent()
}
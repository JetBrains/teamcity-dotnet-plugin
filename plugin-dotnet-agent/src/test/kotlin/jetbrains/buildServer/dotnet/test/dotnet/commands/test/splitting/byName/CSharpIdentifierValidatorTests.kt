

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.splitting.byName

import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.CSharpIdentifierValidator
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class CSharpIdentifierValidatorTests {
    private val validator = CSharpIdentifierValidator()

    @DataProvider
    fun testData() = arrayOf(
        arrayOf("Namespace", false),
        arrayOf("namespace", false),
        arrayOf("Неймспейс", false),
        arrayOf("程序", false),
        arrayOf("неймспейс", false),
        arrayOf("5", false),
        arrayOf("42", false),
        arrayOf("422", false),
        arrayOf("n", false),
        arrayOf("na", false),
        arrayOf("nam", false),
        arrayOf("_Namespace", false),
        arrayOf("3Namepace", false),
        arrayOf(".Namespace", false),
        arrayOf("Namespace_", false),
        arrayOf("Namepace3", false),
        arrayOf("Namespace.", false),
        arrayOf("Namespace.TestClass", true),
        arrayOf("Неймспейс.ТестКласс", true),
        arrayOf("程序.班级", true),
        arrayOf("Namespace.TestClass.", false),
        arrayOf("Name_space.Test_Class", true),
        arrayOf("1Namespace.TestClass", false),
        arrayOf("_Namespace.TestClass", true),
        arrayOf(".Namespace.TestClass", false),
        arrayOf("N?amespace.TestClass", false),
        arrayOf("N#amespace.TestClass", false),
        arrayOf("Namespace.TestClass.TestMethod", true),
        arrayOf("Неймспейс.ТестКласс.ТестМетод", true),
        arrayOf("程序.班级.方法", true),
        arrayOf("Namespace.TestClass.TestMethod.", false),
        arrayOf("Namespace.TestClass.TestMethod_", true),
        arrayOf("Namespace.TestClass..TestMethod", false),
        arrayOf("Namespace.Test Class.TestMethod", false),
        arrayOf("Namespace .TestClass.TestMethod", false),
        arrayOf("Namespace. TestClass.TestMethod", false),
        arrayOf("Namespace . TestClass . TestMethod", false),
        arrayOf("(parameter:", false),
        arrayOf("\"value\",", false),
        arrayOf("\"value2\")", false),
        arrayOf("42,", false),
        arrayOf("42)", false),
        arrayOf("Namespace.TestClass.TestMethod(", false),
        arrayOf("Namespace.TestClass.TestMethod(parameter", false),
    )

    @Test(dataProvider = "testData")
    fun `should validate test name in different scenarios`(testName: String, expected: Boolean) {
        // act
        val actual = validator.isValid(testName)

        // assert
        Assert.assertEquals(actual, expected)
    }
}
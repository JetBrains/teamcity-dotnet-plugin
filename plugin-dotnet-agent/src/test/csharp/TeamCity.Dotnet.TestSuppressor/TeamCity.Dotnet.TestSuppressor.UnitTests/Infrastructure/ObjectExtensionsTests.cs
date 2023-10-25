using TeamCity.Dotnet.TestSuppressor.Infrastructure;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure;

public class ObjectExtensionsTests
{
    private class TestClass
    {
        public string TestProperty { get; set; } = "TestValue";
    }

    [Fact]
    public void GetPropertyValue_WithValidProperty_ReturnsValue()
    {
        // arrange
        var testObj = new TestClass();

        // act
        var result = testObj.GetPropertyValue<string>("TestProperty");

        // assert
        Assert.Equal("TestValue", result);
    }

    [Fact]
    public void GetPropertyValue_WithInvalidProperty_ReturnsNull()
    {
        // arrange
        var testObj = new TestClass();

        // act
        var result = testObj.GetPropertyValue<string>("NonExistentProperty");

        // assert
        Assert.Null(result);
    }

    [Fact]
    public void GetPropertyValue_WithNullObject_ThrowsArgumentNullException()
    {
        // arrange
        TestClass? testObj = null;

        // act, assert
        Assert.Throws<ArgumentNullException>(() => testObj!.GetPropertyValue<string>("TestProperty"));
    }
}

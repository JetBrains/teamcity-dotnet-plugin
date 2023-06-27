using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.CommandLine.Validation;

public class ValidateEnumAttributeTests
{
    private enum TestEnum
    {
        Value1,
        Value2,
        Value3
    }

    [Fact]
    public void IsValid_WithValidEnumValue_ShouldReturnValidResult()
    {
        // arrange
        var attribute = new ValidateEnumAttribute(typeof(TestEnum));

        // act
        var result = attribute.Validate(TestEnum.Value1);

        // assert
        Assert.True(result.IsValid);
        Assert.Empty(result.ErrorMessage);
    }

    [Fact]
    public void IsValid_WithInvalidEnumValue_ShouldReturnInvalidResult()
    {
        // arrange
        var attribute = new ValidateEnumAttribute(typeof(TestEnum));

        // act
        var result = attribute.Validate("InvalidValue");

        // assert
        Assert.False(result.IsValid);
        Assert.Equal("Value must be one of the following: Value1, Value2, Value3", result.ErrorMessage);
    }
}

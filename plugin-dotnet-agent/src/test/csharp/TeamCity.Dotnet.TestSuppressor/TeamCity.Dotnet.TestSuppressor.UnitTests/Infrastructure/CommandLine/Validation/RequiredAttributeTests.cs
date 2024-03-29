using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.CommandLine.Validation;

public class RequiredAttributeTests
{
    [Fact]
    public void IsValid_WithNonNullValue_ShouldReturnValidResult()
    {
        // arrange
        var attribute = new RequiredAttribute("Test error message");

        // act
        var result = attribute.Validate("some value");

        // assert
        Assert.True(result.IsValid);
        Assert.Empty(result.ErrorMessage);
    }

    [Fact]
    public void IsValid_WithNullValue_ShouldReturnInvalidResult()
    {
        // arrange
        var attribute = new RequiredAttribute("Test error message");

        // act
        var result = attribute.Validate(null!);

        // assert
        Assert.False(result.IsValid);
        Assert.Equal("The setting is required: Test error message", result.ErrorMessage);
    }
}

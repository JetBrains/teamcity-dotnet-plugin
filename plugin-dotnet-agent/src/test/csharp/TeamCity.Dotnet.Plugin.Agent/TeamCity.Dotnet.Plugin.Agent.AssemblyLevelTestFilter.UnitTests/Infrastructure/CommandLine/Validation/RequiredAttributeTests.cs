using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.CommandLine.Validation;

public class RequiredAttributeTests
{
    [Fact]
    public void IsValid_WithNonNullValue_ShouldReturnValidResult()
    {
        // arrange
        var attribute = new RequiredAttribute("Test error message");

        // act
        var result = attribute.IsValid("some value");

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
        var result = attribute.IsValid(null!);

        // assert
        Assert.False(result.IsValid);
        Assert.Equal("The setting is required: Test error message", result.ErrorMessage);
    }
}

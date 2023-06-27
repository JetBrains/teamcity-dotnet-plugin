using System.Globalization;
using TeamCity.Dotnet.TestSuppressor.Infrastructure;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure;

public class VerbosityTypeConverterTests
{
    private readonly VerbosityTypeConverter _converter = new();

    [Theory]
    [InlineData("q", Verbosity.Quiet)]
    [InlineData("quiet", Verbosity.Quiet)]
    [InlineData("min", Verbosity.Minimal)]
    [InlineData("minimal", Verbosity.Minimal)]
    [InlineData("n", Verbosity.Normal)]
    [InlineData("normal", Verbosity.Normal)]
    [InlineData("d", Verbosity.Detailed)]
    [InlineData("detailed", Verbosity.Detailed)]
    [InlineData("diag", Verbosity.Diagnostic)]
    [InlineData("diagnostic", Verbosity.Diagnostic)]
    public void ConvertFrom_ValidInput_ExpectedVerbosity(string input, Verbosity expectedVerbosity)
    {
        // act
        var result = _converter.ConvertFrom(null, CultureInfo.InvariantCulture, input);

        // assert
        Assert.NotNull(result);
        Assert.IsType<Verbosity>(result);
        Assert.Equal(expectedVerbosity, (Verbosity)result);
    }

    [Theory]
    [InlineData("INVALID", Verbosity.Normal)]
    [InlineData("", Verbosity.Normal)]
    public void ConvertFrom_InvalidInput_NormalVerbosity(string input, Verbosity expectedVerbosity)
    {
        // act
        var result = _converter.ConvertFrom(null, CultureInfo.InvariantCulture, input);

        // assert
        Assert.NotNull(result);
        Assert.IsType<Verbosity>(result);
        Assert.Equal(expectedVerbosity, (Verbosity)result);
    }

    [Theory]
    [InlineData(typeof(string), true)]
    [InlineData(typeof(int), false)]
    public void CanConvertFrom_Type_ExpectedResult(Type type, bool expected)
    {
        // act
        var result = _converter.CanConvertFrom(null, type);
        
        // assert
        Assert.Equal(expected, result);
    }
}

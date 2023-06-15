using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.Console;

public class AsciiColorExtensionsTests
{
    [Theory]
    [InlineData("Hello, World!", "\x1b[39mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Default))]
    [InlineData("Hello, World!", "\x1b[30mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Black))]
    [InlineData("Hello, World!", "\x1b[31mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Red))]
    [InlineData("Hello, World!", "\x1b[32mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Green))]
    [InlineData("Hello, World!", "\x1b[33mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Yellow))]
    [InlineData("Hello, World!", "\x1b[34mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Blue))]
    [InlineData("Hello, World!", "\x1b[35mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Magenta))]
    [InlineData("Hello, World!", "\x1b[36mHello, World!\x1b[39m", nameof(AsciiColorExtensions.Cyan))]
    [InlineData("Hello, World!", "\x1b[37mHello, World!\x1b[39m", nameof(AsciiColorExtensions.White))]
    public void ExtensionMethods_ShouldReturnFormattedString(string message, string expectedResult, string methodName)
    {
        // arrange, act
        var result = methodName switch
        {
            nameof(AsciiColorExtensions.Default) => message.Default(),
            nameof(AsciiColorExtensions.Black) => message.Black(),
            nameof(AsciiColorExtensions.Red) => message.Red(),
            nameof(AsciiColorExtensions.Green) => message.Green(),
            nameof(AsciiColorExtensions.Yellow) => message.Yellow(),
            nameof(AsciiColorExtensions.Blue) => message.Blue(),
            nameof(AsciiColorExtensions.Magenta) => message.Magenta(),
            nameof(AsciiColorExtensions.Cyan) => message.Cyan(),
            nameof(AsciiColorExtensions.White) => message.White(),
            _ => throw new ArgumentException($"Invalid method name: {methodName}")
        };

        // assert
        Assert.Equal(expectedResult, result);
    }
}



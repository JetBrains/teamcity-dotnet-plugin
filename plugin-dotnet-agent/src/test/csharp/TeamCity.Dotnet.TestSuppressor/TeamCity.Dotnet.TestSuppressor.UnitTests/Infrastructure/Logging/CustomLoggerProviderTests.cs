using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Infrastructure;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.Logging;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.Logging;

public class CustomLoggerProviderTests
{
    [Fact]
    public void CreateLogger_UsesCorrectConfigurator()
    {
        // arrange
        var commandMock = new Mock<Command>
        {
            Object =
            {
                Verbosity = Verbosity.Detailed
            }
        };

        var optionsMock = new Mock<IOptions<Command>>();
        optionsMock.Setup(o => o.Value).Returns(commandMock.Object);

        var normalConfiguratorMock = new Mock<ILoggerConfigurator>();
        normalConfiguratorMock.Setup(c => c.Verbosity).Returns(Verbosity.Normal);

        var detailedConfiguratorMock = new Mock<ILoggerConfigurator>();
        detailedConfiguratorMock.Setup(c => c.Verbosity).Returns(Verbosity.Detailed);

        var loggerConfigurators = new[] { normalConfiguratorMock.Object, detailedConfiguratorMock.Object };

        var customLoggerProvider = new CustomLoggerProvider<Command>(optionsMock.Object, loggerConfigurators);

        // act
        customLoggerProvider.CreateLogger("Test");

        // assert
        normalConfiguratorMock.Verify(c => c.Configure(It.IsAny<ILoggingBuilder>()), Times.Never);
        detailedConfiguratorMock.Verify(c => c.Configure(It.IsAny<ILoggingBuilder>()), Times.Once);
    }
    
    [Fact]
    public void CreateLogger_UsesNormalConfigurator_WhenNoMatchingConfiguratorFound()
    {
        // arrange
        var commandMock = new Mock<Command>
        {
            Object =
            {
                Verbosity = Verbosity.Diagnostic
            }
        };

        var optionsMock = new Mock<IOptions<Command>>();
        optionsMock.Setup(o => o.Value).Returns(commandMock.Object);

        var normalConfiguratorMock = new Mock<ILoggerConfigurator>();
        normalConfiguratorMock.Setup(c => c.Verbosity).Returns(Verbosity.Normal);

        var detailedConfiguratorMock = new Mock<ILoggerConfigurator>();
        detailedConfiguratorMock.Setup(c => c.Verbosity).Returns(Verbosity.Detailed);

        var loggerConfigurators = new[] { normalConfiguratorMock.Object, detailedConfiguratorMock.Object };

        var customLoggerProvider = new CustomLoggerProvider<Command>(optionsMock.Object, loggerConfigurators);

        // act
        customLoggerProvider.CreateLogger("Test");

        // assert
        normalConfiguratorMock.Verify(c => c.Configure(It.IsAny<ILoggingBuilder>()), Times.Once);
        detailedConfiguratorMock.Verify(c => c.Configure(It.IsAny<ILoggingBuilder>()), Times.Never);
    }

    [Fact]
    public void CreateLogger_UsesNormalConfigurator_WhenVerbosityNotSpecified()
    {
        // arrange
        var commandMock = new Mock<Command>();

        var optionsMock = new Mock<IOptions<Command>>();
        optionsMock.Setup(o => o.Value).Returns(commandMock.Object);

        var normalConfiguratorMock = new Mock<ILoggerConfigurator>();
        normalConfiguratorMock.Setup(c => c.Verbosity).Returns(Verbosity.Normal);

        var detailedConfiguratorMock = new Mock<ILoggerConfigurator>();
        detailedConfiguratorMock.Setup(c => c.Verbosity).Returns(Verbosity.Detailed);

        var loggerConfigurators = new[] { normalConfiguratorMock.Object, detailedConfiguratorMock.Object };

        var customLoggerProvider = new CustomLoggerProvider<Command>(optionsMock.Object, loggerConfigurators);

        // act
        customLoggerProvider.CreateLogger("Test");

        // assert
        normalConfiguratorMock.Verify(c => c.Configure(It.IsAny<ILoggingBuilder>()), Times.Once);
        detailedConfiguratorMock.Verify(c => c.Configure(It.IsAny<ILoggingBuilder>()), Times.Never);
    }

}
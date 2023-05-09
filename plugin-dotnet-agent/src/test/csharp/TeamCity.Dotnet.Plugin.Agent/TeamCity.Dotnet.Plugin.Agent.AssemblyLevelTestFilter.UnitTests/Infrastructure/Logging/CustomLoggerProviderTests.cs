/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Logging;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.Logging;

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
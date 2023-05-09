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
using Moq;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Help;
// ReSharper disable TemplateIsNotCompileTimeConstantProblem

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.CommandLine.Help;

public class HelpPrinterTests
{
    [Fact]
    public void PrintHelp_ShouldPrintHelpText()
    {
        // arrange
        var loggerMock = new Mock<ILogger<HelpPrinter>>();

        var helpPrinter = new HelpPrinter(loggerMock.Object);
        var testCommand = new TestCommand();

        // act
        helpPrinter.PrintHelp(testCommand);

        // assert
        loggerMock
            .VerifyLogging("Available commands and options:", LogLevel.Information)
            .VerifyLogging("    list                List available items                                                                            ", LogLevel.Information)
            .VerifyLogging("    add                 Add new item                                                                                    ", LogLevel.Information)
            .VerifyLogging("    --name | -n         Specify item name                                                                               ", LogLevel.Information)
            .VerifyLogging("    --desc | -d         Specify item description                                                                        ", LogLevel.Information)
            .VerifyLogging("    -h | --help | -?    Display help information                                                                        ", LogLevel.Information)
            .VerifyLogging("    -v | --verbosity    Verbosity of output. Possible values: q[uiet], min[imal], n[ormal], det[ailed], diag[nostic]    ", LogLevel.Information);
    }

    private class TestCommand : Command
    {
        [Command("list"), CommandDescription("List available items")]
        public object? ListCommand { get; set; }

        [Command("add"), CommandDescription("Add new item")]
        public object? AddCommand { get; set; }

        [CommandOption(false, "--name", "-n"), CommandOptionDescription("Specify item name")]
        public string? NameOption { get; set; }

        [CommandOption(false, "--desc", "-d"), CommandOptionDescription("Specify item description")]
        public string? DescriptionOption { get; set; }
    }
}

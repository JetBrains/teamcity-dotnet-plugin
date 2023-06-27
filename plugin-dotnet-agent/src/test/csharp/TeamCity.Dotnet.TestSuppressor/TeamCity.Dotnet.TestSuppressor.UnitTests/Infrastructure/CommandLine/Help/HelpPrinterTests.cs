using Microsoft.Extensions.Logging;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Help;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.CommandLine.Help;

public class HelpPrinterTests
{
    [Fact]
    public void PrintHelp_ShouldPrintHelpText()
    {
        var loggerMock = new Mock<ILogger<HelpPrinter>>();

        var helpPrinter = new HelpPrinter(loggerMock.Object);
        var testCommand = new TestCommand();

        helpPrinter.PrintHelp(testCommand);

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

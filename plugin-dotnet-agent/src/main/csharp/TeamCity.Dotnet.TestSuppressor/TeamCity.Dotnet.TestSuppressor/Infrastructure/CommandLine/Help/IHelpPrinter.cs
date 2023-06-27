using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Help;

internal interface IHelpPrinter
{
    void PrintHelp(Command command);
}
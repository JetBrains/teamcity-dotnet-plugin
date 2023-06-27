namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Parsing;

internal record CommandLineParsingResult(
    IDictionary<string, string> Mappings,
    IList<string> UnknownParameters
) : IConfigurationParsingResult;
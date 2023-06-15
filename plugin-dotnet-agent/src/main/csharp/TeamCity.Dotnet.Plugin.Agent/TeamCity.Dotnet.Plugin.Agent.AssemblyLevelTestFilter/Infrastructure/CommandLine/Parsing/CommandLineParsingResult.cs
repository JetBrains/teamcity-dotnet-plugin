namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Parsing;

internal record CommandLineParsingResult(
    IDictionary<string, string> Mappings,
    IList<string> UnknownParameters
) : IConfigurationParsingResult;
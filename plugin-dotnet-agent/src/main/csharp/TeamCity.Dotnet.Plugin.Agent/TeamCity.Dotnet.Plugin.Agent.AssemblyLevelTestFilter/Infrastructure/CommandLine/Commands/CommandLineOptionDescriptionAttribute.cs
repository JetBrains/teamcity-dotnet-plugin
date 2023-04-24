namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

[AttributeUsage(AttributeTargets.Property)]
public class CommandLineOptionDescriptionAttribute : Attribute
{
    public string Description { get; set; }

    public CommandLineOptionDescriptionAttribute(string description)
    {
        Description = description;
    }
}
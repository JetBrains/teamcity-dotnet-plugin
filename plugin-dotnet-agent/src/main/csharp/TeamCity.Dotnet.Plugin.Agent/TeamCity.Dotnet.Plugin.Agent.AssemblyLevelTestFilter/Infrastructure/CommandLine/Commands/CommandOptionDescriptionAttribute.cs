namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

[AttributeUsage(AttributeTargets.Property)]
public class CommandOptionDescriptionAttribute : Attribute
{
    public string Description { get; set; }

    public CommandOptionDescriptionAttribute(string description)
    {
        Description = description;
    }
}
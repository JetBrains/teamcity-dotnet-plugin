namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

[AttributeUsage(AttributeTargets.Property)]
public class CommandAttribute : Attribute
{
    public string Command { get; }

    public CommandAttribute(string command)
    {
        Command = command;
    }
}
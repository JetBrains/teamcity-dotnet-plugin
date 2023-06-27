using System.Collections;
using System.Reflection;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;

namespace TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Parsing;

internal class CommandLineParser<TCommand> : ICommandLineParser<TCommand>
    where TCommand : Command
{
    private const string True = "true";
    private readonly Type _commandType;

    public CommandLineParser()
    {
        _commandType = typeof(TCommand);
    }
    
    // we need to assemble KV pairs from command line arguments corresponding to command options, e.g.
    // RootCommand:SubCommand1:SubCommand2:IsActive     -->     true  – it's necessary to instantiate command object
    // RootCommand:SubCommand1:Path                     -->     /path/from/command/line
    // RootCommand:Help                                 -->     true
    // etc
    public IConfigurationParsingResult Parse(IEnumerable<string> args)
    {
        var commandPath = new List<string> { _commandType.Name };
        
        var mappingsResult = new Dictionary<string, string>
        {
            { Key(commandPath, nameof(Command.IsActive)), True }  // RootCommand:IsActive --> true – always true for command
        };
        var unknownArguments = new List<string>();
        var arguments = new Queue<string>(args);
        var commandType = _commandType;
        var prevKey = string.Empty;
        
        // we have 5 possibilities for every single argument:
        // - it's a command
        // - it's an option required value
        // - it's a value for option defined in previous argument
        // - it's an option flag
        // - it's an unknown argument
        while (arguments.TryDequeue(out var argument))
        {
            var properties = commandType.GetProperties();
            
            // if prev argument was option required value – current argument is value
            if (!string.IsNullOrWhiteSpace(prevKey) && !IsArgumentOption(properties, argument) && !IsArgumentCommand(properties, argument))
            {
                mappingsResult.Add(prevKey, argument);
                prevKey = string.Empty;    // reset key and value
                continue;
            }

            var (isOption, key) = TryParseOption(properties, argument, commandPath, mappingsResult);
            if (isOption)
            {
                if (key != string.Empty)
                {
                    prevKey = key;
                }
                continue;
            }

            var (isCommand, cmdType) = TryParseCommand(properties, argument, mappingsResult, commandPath);
            if (isCommand)
            {
                if (cmdType != null)
                {
                    commandType = cmdType;
                }
                continue;
            }
            
            // unknown argument
            unknownArguments.Add(argument);
        }
        
        if (commandPath.Count <= 1)
        {
            return new CommandLineParsingResult(mappingsResult, unknownArguments);
        }
        
        // if there is a several commands in command path
        // put verbosity value to the root level command since it's valid only for root level command
        if (mappingsResult.TryGetValue(Key(commandPath, nameof(Command.Verbosity)), out var verbosity))
        {
            mappingsResult.Add(Key(new[] { _commandType.Name }, nameof(Command.Verbosity)), verbosity);
        }

        return new CommandLineParsingResult(mappingsResult, unknownArguments);
    }

    private static (bool, Type?) TryParseCommand(
        IEnumerable<PropertyInfo> properties, string argument, IDictionary<string, string> mappingsResult, ICollection<string> commandPath)
    {
        var isCommand = false;
        Type? commandType = null;
        foreach (var (commandProperty, commandAttribute) in OnlyWithAttribute<CommandAttribute>(properties))
        {
            // if current argument is a command...
            if (commandAttribute.Command != argument.ToLowerInvariant())
            {
                continue;
            }

            isCommand = true;

            // 1. set IsActive == true for current command to instantiate command object
            mappingsResult.Add(Key(commandPath, commandProperty.Name, nameof(Command.IsActive)), True);

            // 2. set current command type as type of the command add command name to command path
            // (go deeper in command tree)
            commandType = commandProperty.PropertyType;
            commandPath.Add(commandProperty.Name);

            break;
        }

        return (isCommand, commandType);
    }

    private static (bool, string) TryParseOption(
        IEnumerable<PropertyInfo> properties, string argument, IEnumerable<string> commandPath, IDictionary<string, string> mappingsResult)
    {
        var isOption = false;
        var key = string.Empty;
        foreach (var (optionProperty, optionAttribute) in OnlyWithAttribute<CommandOptionAttribute>(properties))
        {
            // if current argument is an option...
            if (optionAttribute.Options.All(o => o != argument.ToLowerInvariant()))
            {
                continue;
            }

            isOption = true;

            // ...and requires value – next argument is value
            if (optionAttribute.RequiresValue)
            {
                key = Key(commandPath, optionProperty.Name);
                
                // if option is array – add index to key
                if (optionProperty.PropertyType.IsArray)
                {
                    var index = mappingsResult.Count(kv => kv.Key.StartsWith($"{key}:"));
                    key += $":{index}";
                }
            }
            else
            {
                // ...and doesn't require value – it's a flag option – set true
                mappingsResult.Add(Key(commandPath, optionProperty.Name), True);
            }

            break;
        }

        return (isOption, key);
    }

    private static bool IsArgumentCommand(IEnumerable<PropertyInfo> properties, string argument) =>
        OnlyWithAttribute<CommandAttribute>(properties)
            .Select(x => x.Item2)
            .Any(a => a.Command == argument.ToLowerInvariant());

    private static bool IsArgumentOption(IEnumerable<PropertyInfo> properties, string argument) =>
        OnlyWithAttribute<CommandOptionAttribute>(properties)
            .Select(x => x.Item2)
            .SelectMany(a => a.Options)
            .Any(o => o == argument.ToLowerInvariant());

    private static IEnumerable<(PropertyInfo, TAttribute)> OnlyWithAttribute<TAttribute>(IEnumerable<PropertyInfo> properties) where TAttribute : Attribute =>
        properties
            .Select(p => (p, p.GetCustomAttribute<TAttribute>()))
            .Where(x => x.Item2 != null)
            .Select(x => (x.Item1, x.Item2!));
    
    private static string Key(IEnumerable<string> commandPath, params string[] segments)
    {
        var allSegments = commandPath.ToList();
        allSegments.AddRange(segments);
        return string.Join(':', allSegments);
    }
}
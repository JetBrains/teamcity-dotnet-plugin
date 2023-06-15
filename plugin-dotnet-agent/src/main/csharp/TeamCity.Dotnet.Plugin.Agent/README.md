# TeamCity.Dotnet.Plugin.Agent

This console application, developed using .NET and Mono.Cecil, is designed to suppress tests for parallel testing in TeamCity. It provides functionality to suppress and restore test assemblies based on a target path and a test list file.

## Features

- **Suppress Tests**: The application allows you to suppress tests by specifying the target path and a file containing a list of test classes.
- **Restore Original Assemblies**: You can restore the original versions of the suppressed assemblies by providing the path to a CSV file containing the metadata after suppression.

## Usage

The application provides two commands:

1. **Suppress Command**: This command allows you to suppress tests by executing the following command:
```
dotnet ./TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.dll suppress --target <path_to_target> --test-list <path_to_test_list_file>
```
- `<path_to_target>`: The target path where the test assemblies, .csproj, .sln or directory are located.
- `<path_to_test_list_file>`: The path to the file (.txt) containing the list of test classes.

2. **Restore Command**: This command enables you to restore the original assemblies by executing the following command:
```
dotnet ./TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.dll restore --metadata-file <path_to_metadata_file>
```
- `<path_to_metadata_file>`: The path to the CSV file containing the metadata of the suppressed assemblies.

## Restrictions

– Currently supported only .NET 6.0.xxx, 7.0.xxx, 8.0.xxx
– Target resolution for projects works only for Debug tests mode
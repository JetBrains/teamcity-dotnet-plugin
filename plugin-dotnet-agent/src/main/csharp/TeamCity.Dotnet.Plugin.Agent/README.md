# TeamCity.Dotnet.Plugin.Agent

This console application, developed using .NET and Mono.Cecil, is designed to suppress tests for parallel testing in TeamCity. It provides functionality to suppress and restore test assemblies based on a target path and a test list file.

## Features

- **Suppress Tests**: The application allows you to suppress tests by specifying the target path and a file containing a list of test classes. After the suppressing this command generates metadata CSV-file that contains semicolon separeted list of patched assemblies
- **Restore Original Assemblies**: You can restore the original versions of the suppressed assemblies by providing the path to a CSV file containing the metadata after suppression

## Usage

The application provides two commands:

1. **Suppress Command**: This command allows you to suppress tests by executing the following command:
```
dotnet ./TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.dll \
  suppress \
  --target <PATH_TO_TARGET> \
  --test-list <PATH_TO_TEST_LIST_FILE> \
  --backup <PATH_TO_OUTPUT_METADATA_FILE> \
  --inclusion-mode
```
- `<PATH_TO_TARGET>` – the target path where the test assemblies (.dll, .exe), .csproj, .sln or directory are located
- `<PATH_TO_TEST_LIST_FILE>` – the path to the file (.txt) containing the list of test classes
– `<PATH_TO_OUTPUT_METADATA_FILE>` – the path to the output metadata file (.csv) contains the semicolon-separated list of patched assemblies
– `--inclusion-mode` – the flag indicates that should be suppressed all tests except ones from the test list file. Otherwise, the application works in exclusion mode: all the tests from the test list file should be suppressed.

The test list .txt file have the following format and usually provided by TeamCity parallel test feature:
```
# some comments
# ...
# some control statements that doesn't matter in that application
# ...

NamespaceA.NamespaceB.NamespaceC.TestClass0
NamespaceA.NamespaceB.TestClass1
NamespaceA.TestClass2
NamespaceA.NamespaceB.NamespaceC.TestClass3(param1, param2)
NamespaceA.NamespaceB.TestClass(param1)

# ...
```

The metadata .csv file have the following format:
```
"/absolut/path/to/the/patched/assembly1.dll";"/absolut/path/to/the/original/assemly1.dll_backup"
"/absolut/path/to/the/patched/assembly2.dll";"/absolut/path/to/the/original/assemly2.dll_backup"
"/absolut/path/to/the/patched/assembly3.dll";"/absolut/path/to/the/original/assemly3.dll_backup"
```

2. **Restore Command**: This command enables you to restore the original assemblies by executing the following command:
```
dotnet ./TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.dll \
  restore \
  --backup-metadata <PATH_METADATA_FILE>
```
- `<PATH_METADATA_FILE>`: The path to the CSV file containing the metadata of the suppressed assemblies.

## Restrictions

– Currently supported only .NET 6.0.xxx, 7.0.xxx, 8.0.xxx
– Target resolution for projects works only for Debug tests mode
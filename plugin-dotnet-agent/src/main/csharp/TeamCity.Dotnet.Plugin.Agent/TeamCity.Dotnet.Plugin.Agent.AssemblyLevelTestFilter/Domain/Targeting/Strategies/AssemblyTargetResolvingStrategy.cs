/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

using Microsoft.Extensions.Logging;
using Mono.Cecil;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting.Strategies;

internal class AssemblyTargetResolvingStrategy : BaseTargetResolvingStrategy, ITargetResolvingStrategy
{
    private readonly ILogger<AssemblyTargetResolvingStrategy> _logger;
    private readonly IReadOnlyList<ITestEngine> _testEngines;

    public override TargetType TargetType => TargetType.Assembly;

    public AssemblyTargetResolvingStrategy(
        IFileSystem fileSystem,
        ILogger<AssemblyTargetResolvingStrategy> logger,
        IEnumerable<ITestEngine> testEngines) : base(fileSystem, logger)
    {
        _logger = logger;
        _testEngines = testEngines.ToList();
    }

    protected override IEnumerable<string> AllowedTargetExtensions => new [] { FileExtension.Dll, FileExtension.Exe };

    public override IEnumerable<(FileSystemInfo, TargetType)> Resolve(string target)
    {
        _logger.LogInformation("Resolving target assembly: {Target}", target);

        var assemblyFile = TryToGetTargetFile(target);
        if (assemblyFile == null)
        {
            _logger.LogWarning("Invalid assembly target file: {Target}", target);
            yield break;
        }

        var (isAssembly, detectedEngines) = TryDetectEngines(assemblyFile);
        if (!isAssembly)
        {
            _logger.LogDebug("Target assembly is not a .NET assembly: {Target}", target);
            yield break;
        }
        if (detectedEngines == null || detectedEngines.Length == 0)
        {
            _logger.LogDebug("Target assembly doesn't contain tests written on supported test frameworks: {Target}", target);
            yield break;
        }
        
        _logger.LogInformation("Assembly {Target} depends on following test frameworks: {Engines}", target, string.Join(", ", detectedEngines));
        _logger.LogInformation("Resolved assembly: {Assembly}", assemblyFile.FullName);
        yield return (assemblyFile, TargetType.Assembly);
    }

    private (bool isAssembly, string[]? detectedEngines) TryDetectEngines(FileSystemInfo file)
    {
        try
        {
            using var assembly = AssemblyDefinition.ReadAssembly(file.FullName);
            var assemblyReferences = assembly.MainModule.AssemblyReferences;
            if (assemblyReferences == null || assemblyReferences.Count == 0)
            {
                return (true, null);
            }
            
            _logger.LogDebug("Examine assembly {Assembly} references:\n\t\t\t\t{AssemblyAttrs}", 
                file, string.Join("\n\t\t\t\t", assemblyReferences.Select(a => a.FullName)));

            var detectedEngines = _testEngines
                .Where(te => te.AssembliesNames.Any(tca => assemblyReferences.Any(a => a.Name == tca)))
                .Select(te => te.Name)
                .ToArray();
            return (true, detectedEngines);
        }
        catch (BadImageFormatException)
        {
            _logger.LogWarning("Can't read target assembly definition: {Target}", file.FullName);
            return (false, null);
        }
    }
}
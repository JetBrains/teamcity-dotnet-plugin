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

using System.IO.Abstractions;
using Microsoft.Extensions.Logging;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.FS;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal class TargetResolver : ITargetResolver
{
    private readonly IDictionary<TargetType, ITargetResolvingStrategy> _strategies;
    private readonly IFileSystem _fileSystem;
    private readonly ILogger<TargetResolver> _logger;

    public TargetResolver(
        IEnumerable<ITargetResolvingStrategy> strategies,
        IFileSystem fileSystem,
        ILogger<TargetResolver> logger)
    {
        _strategies = strategies.ToDictionary(s => s.TargetType);
        _fileSystem = fileSystem;
        _logger = logger;
    }
    
    private ITargetResolvingStrategy AssemblyStrategy => _strategies[TargetType.Assembly];

    public IEnumerable<IFileInfo> Resolve(string target)
    {
        _logger.LogInformation("Resolving target: {Target}", target);

        var (originalTargetPath, exception) = _fileSystem.GetFileSystemInfo(target);
        if (exception != null)
        {
            _logger.LogError(exception, "Target not available: {Target}", target);
            throw new FileNotFoundException($"Target '{target}' not available");
        }
        
        var supposedTargetType = SpeculateTargetType(originalTargetPath!);
        
        // if target is an assembly, we can process once and return it right away
        if (supposedTargetType == TargetType.Assembly)
        {
            foreach (var (resolvedAssembly, _) in AssemblyStrategy.Resolve(originalTargetPath!.FullName))
            {
                yield return (IFileInfo) resolvedAssembly;
            }
            yield break;
        }

        // if target is not an assembly, resolve all targets in the hierarchy using BFS
        var queue = new Queue<(IFileSystemInfo, TargetType)>();
        
        queue.Enqueue((originalTargetPath!, supposedTargetType));
        while (queue.Count != 0)
        {
            var (currentFileSystemInfo, targetType) = queue.Dequeue();
            if (!_strategies.TryGetValue(targetType, out var strategy))
            {
                _logger.LogError("No target resolution strategy for target type: {TargetType}", targetType);
                continue;
            }
            
            foreach (var (resolvedTargetFile, resolvedTargetType) in strategy.Resolve(currentFileSystemInfo.FullName))
            {
                if (resolvedTargetType == TargetType.Assembly)
                {
                    foreach (var (resolvedAssembly, _) in AssemblyStrategy.Resolve(resolvedTargetFile.FullName))
                    {
                        yield return (IFileInfo) resolvedAssembly;
                    }
                    continue;
                }
                
                queue.Enqueue((resolvedTargetFile, resolvedTargetType));
            }
        }
    }
    
    private  TargetType SpeculateTargetType(IFileSystemInfo fileSystemInfo)
    {
        if (fileSystemInfo.IsDirectory())
        {
            return TargetType.Directory;
        }
        
        var extension = fileSystemInfo.Extension.ToLowerInvariant();
        
        if (TargetType.Assembly.GetPossibleFileExtension().Contains(extension))
        {
            return TargetType.Assembly;
        }

        if (TargetType.Project.GetPossibleFileExtension().Contains(extension))
        {
            return TargetType.Project;
        }

        if (TargetType.Solution.GetPossibleFileExtension().Contains(extension))
        {
            return TargetType.Solution;
        }

        throw new NotSupportedException($"Unsupported target type: '{extension}'.");
    }
}

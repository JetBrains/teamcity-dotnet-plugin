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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal class TargetResolver : ITargetResolver
{
    private readonly IDictionary<TargetType, ITargetResolvingStrategy> _strategies;
    private readonly ILogger<TargetResolver> _logger;

    public TargetResolver(IEnumerable<ITargetResolvingStrategy> strategies, ILogger<TargetResolver> logger)
    {
        _strategies = strategies.ToDictionary(s => s.TargetType);
        _logger = logger;
    }
    
    private ITargetResolvingStrategy AssemblyStrategy => _strategies[TargetType.Assembly];

    public IEnumerable<FileInfo> Resolve(string target)
    {
        _logger.LogInformation("Resolving target: {Target}", target);

        var originalTargetFile = new FileInfo(target);
        if (!originalTargetFile.Exists && !Directory.Exists(target))
        {
            _logger.LogError("Target not found: {Target}", target);
            throw new FileNotFoundException($"Target '{target}' not found.");
        }
        
        var supposedTargetType = SpeculateTargetType(originalTargetFile);

        // resolve all targets in the hierarchy using BFS
        var queue = new Queue<(FileInfo, TargetType)>();
        
        queue.Enqueue((originalTargetFile, supposedTargetType));
        while (queue.Count != 0)
        {
            var (targetFile, targetType) = queue.Dequeue();
            foreach (var (resolvedTargetFile, resolvedTargetType) in _strategies[targetType].Resolve(targetFile.FullName))
            {
                if (resolvedTargetType == TargetType.Assembly)
                {
                    foreach(var (resolvedAssembly, _) in AssemblyStrategy.Resolve(resolvedTargetFile.FullName))
                    {
                        yield return resolvedAssembly;
                    }
                    continue;
                }
                
                queue.Enqueue((resolvedTargetFile, resolvedTargetType));
            }
        }
    }

    private static TargetType SpeculateTargetType(FileSystemInfo fileInfo)
    {
        if (fileInfo.Attributes.HasFlag(FileAttributes.Directory))
        {
            return TargetType.Directory;
        }
        
        var extension = fileInfo.Extension.ToLowerInvariant();
        
        if (extension == TargetType.Assembly.FileExtension())
        {
            return TargetType.Assembly;
        }

        if (extension == TargetType.Project.FileExtension())
        {
            return TargetType.Project;
        }

        if (extension == TargetType.Solution.FileExtension())
        {
            return TargetType.Solution;
        }

        throw new NotSupportedException($"Unsupported target type: '{extension}'.");
    }
}

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

    public IEnumerable<FileInfo> Resolve(string target)
    {
        _logger.LogInformation("Resolving target: {Target}", target);

        var fileInfo = new FileInfo(target);
        if (!fileInfo.Exists)
        {
            _logger.LogError("Target not found: {Target}", target);
            throw new FileNotFoundException($"Target '{target}' not found.");
        }

        var extension = fileInfo.Extension.ToLowerInvariant();
        var targetType = GetTargetType(extension, fileInfo);

        // resolve all targets in the hierarchy
        var queue = new Queue<(FileInfo, TargetType)>();
        queue.Enqueue((fileInfo, targetType));
        while (queue.Count != 0)
        {
            var (file, fileTargetType) = queue.Dequeue();
            if (fileTargetType == TargetType.Assembly)
            {
                _logger.LogInformation("Returning assembly: {Assembly}", file.FullName);
                yield return file;
                continue;
            }
            
            foreach (var (f, t) in _strategies[fileTargetType].FindAssembliesAsync(file.FullName))
            {
                queue.Enqueue((f, t));
            }
        }
    }

    private static TargetType GetTargetType(string extension, FileSystemInfo fileInfo)
    {
        if (fileInfo.Attributes.HasFlag(FileAttributes.Directory))
        {
            return TargetType.Directory;
        }
        
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

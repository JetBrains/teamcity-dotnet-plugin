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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.Targeting;

internal enum TargetType
{
    Directory,
    Solution,
    Project,
    Assembly
}

internal static class TargetTypeExtensions
{
    public static IEnumerable<string> GetPossibleFileExtension(this TargetType targetType)
    {
        switch (targetType)
        {
            case TargetType.Solution:
                yield return Infrastructure.FS.FileExtension.Solution;
                break;
            case TargetType.Project:
                yield return Infrastructure.FS.FileExtension.CSharpProject;
                yield return Infrastructure.FS.FileExtension.VisualBasicProject;
                yield return Infrastructure.FS.FileExtension.FSharpProject;
                yield return Infrastructure.FS.FileExtension.MsBuildProject;
                yield return Infrastructure.FS.FileExtension.MsBuildProject2;
                break;
            case TargetType.Assembly:
                yield return Infrastructure.FS.FileExtension.Dll;
                yield return Infrastructure.FS.FileExtension.Exe;
                break;
            case TargetType.Directory:
                throw new ArgumentOutOfRangeException(nameof(targetType), targetType, "Directory has no file extension");
            default:
                throw new ArgumentOutOfRangeException(nameof(targetType), targetType, "Unknown target type value to get file extension");
        };
    }
}
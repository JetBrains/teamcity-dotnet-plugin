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

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.CommandLine.Commands;

[Serializable]
public abstract class Command
{
    [CommandOption(requiresValue: false, "-h", "--help", "-?")]
    [CommandOptionDescription("Display help information")]
    public bool Help { get; set; } = false;
    
    [CommandOption(requiresValue: true,"-v", "--verbosity")]
    [CommandOptionDescription("Verbosity of output. Possible values: q[uiet], min[imal], n[ormal], det[ailed], diag[nostic]")]
    [ValidateEnum(typeof(Verbosity), errorMessage: "Invalid verbosity value. Possible values: q[uiet], min[imal], n[ormal], d[etailed], diag[nostic]")]
    public Verbosity Verbosity { get; set; } = Verbosity.Normal;
    
    /// <summary>
    /// If true, command will be executed; necessary for execution path resolution
    /// </summary>
    public bool IsActive { get; set; } = false;
}
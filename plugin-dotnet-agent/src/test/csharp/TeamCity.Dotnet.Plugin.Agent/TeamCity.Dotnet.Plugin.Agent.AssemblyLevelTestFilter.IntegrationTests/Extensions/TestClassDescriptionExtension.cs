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

using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Fixtures.TestProjects;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.IntegrationTests.Extensions;

public static class TestClassDescriptionExtension
{
    public static IReadOnlyList<string> GetFullTestMethodsNames(this TestClassDescription[] testClassDescriptions, string projectName) =>
        testClassDescriptions.SelectMany(d => d.TestMethodsNames.Select(m => $"{projectName}.{d.ClassName}.{m}")).ToList();
    
    public static IReadOnlyList<string> GetFullTestMethodsNames(this TestClassDescription testClassDescription, string projectName) =>
        testClassDescription.TestMethodsNames.Select(m => $"{projectName}.{testClassDescription.ClassName}.{m}").ToList();
}
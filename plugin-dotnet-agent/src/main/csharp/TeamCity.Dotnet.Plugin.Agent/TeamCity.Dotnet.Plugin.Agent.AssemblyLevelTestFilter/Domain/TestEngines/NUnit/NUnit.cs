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

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Domain.TestEngines.NUnit;

internal class NUnit : ITestEngine
{
    public string Name => "NUnit";

    public IList<string> TestClassAttributes { get; } = new List<string>
    {
        "NUnit.Framework.TestFixtureAttribute",
        "NUnit.Framework.TestFixtureSourceAttribute"
    };

    public IList<string> TestMethodAttributes { get; } = new List<string>
    {
        "NUnit.Framework.TestAttribute",
        "NUnit.Framework.TestCaseAttribute",
        "NUnit.Framework.TestCaseSourceAttribute"
    };
}
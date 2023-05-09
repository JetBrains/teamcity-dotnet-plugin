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

using System.Globalization;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure;

public class VerbosityTypeConverterTests
{
    private readonly VerbosityTypeConverter _converter = new();

    [Theory]
    [InlineData("q", Verbosity.Quiet)]
    [InlineData("quiet", Verbosity.Quiet)]
    [InlineData("min", Verbosity.Minimal)]
    [InlineData("minimal", Verbosity.Minimal)]
    [InlineData("n", Verbosity.Normal)]
    [InlineData("normal", Verbosity.Normal)]
    [InlineData("d", Verbosity.Detailed)]
    [InlineData("detailed", Verbosity.Detailed)]
    [InlineData("diag", Verbosity.Diagnostic)]
    [InlineData("diagnostic", Verbosity.Diagnostic)]
    public void ConvertFrom_ValidInput_ExpectedVerbosity(string input, Verbosity expectedVerbosity)
    {
        // act
        var result = _converter.ConvertFrom(null, CultureInfo.InvariantCulture, input);

        // assert
        Assert.NotNull(result);
        Assert.IsType<Verbosity>(result);
        Assert.Equal(expectedVerbosity, (Verbosity)result);
    }

    [Theory]
    [InlineData("INVALID", Verbosity.Normal)]
    [InlineData("", Verbosity.Normal)]
    public void ConvertFrom_InvalidInput_NormalVerbosity(string input, Verbosity expectedVerbosity)
    {
        // act
        var result = _converter.ConvertFrom(null, CultureInfo.InvariantCulture, input);

        // assert
        Assert.NotNull(result);
        Assert.IsType<Verbosity>(result);
        Assert.Equal(expectedVerbosity, (Verbosity)result);
    }

    [Theory]
    [InlineData(typeof(string), true)]
    [InlineData(typeof(int), false)]
    public void CanConvertFrom_Type_ExpectedResult(Type type, bool expected)
    {
        // act
        var result = _converter.CanConvertFrom(null, type);
        
        // assert
        Assert.Equal(expected, result);
    }
}

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

using System.Text;
using TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.UnitTests.Infrastructure.Console;

public class ColumnAlignerTests
{
    [Fact]
    public void AddRowAndFlush_AlignedColumnString()
    {
        // Arrange
        var columnAligner = new ColumnAligner('|');
        var stringBuilder = new StringBuilder();
        void Logger(string s) => stringBuilder.AppendLine(s.TrimEnd());

        columnAligner.AddRow("Name|Age|City");
        columnAligner.AddRow("John Doe|25|New York");
        columnAligner.AddRow("Jane Smith|30|Los Angeles");

        const string expectedOutput = @"Name       Age City
John Doe   25  New York
Jane Smith 30  Los Angeles
";

        // Act
        columnAligner.Flush(Logger);
        var actualOutput = stringBuilder.ToString();

        // Assert
        Assert.Equal(expectedOutput, actualOutput);
    }

    [Fact]
    public void AddRowAndFlush_WithCustomSeparatorAndPadding_AlignedColumnString()
    {
        // Arrange
        var columnAligner = new ColumnAligner(';', 2);
        var stringBuilder = new StringBuilder();
        void Logger(string s)
        {
            stringBuilder.AppendLine(s.TrimEnd());
        }

        columnAligner.AddRow("Name;Age;City");
        columnAligner.AddRow("John Doe;25;New York");
        columnAligner.AddRow("Jane Smith;30;Los Angeles");

        const string expectedOutput = @"Name        Age  City
John Doe    25   New York
Jane Smith  30   Los Angeles
";

        // Act
        columnAligner.Flush(Logger);
        var actualOutput = stringBuilder.ToString();

        // Assert
        Assert.Equal(expectedOutput, actualOutput);
    }

    [Fact]
    public void Flush_ClearsRows_AlignedColumnString()
    {
        // Arrange
        var columnAligner = new ColumnAligner('|');
        var stringBuilder = new StringBuilder();
        void Logger(string s) => stringBuilder.AppendLine(s.TrimEnd());

        columnAligner.AddRow("Name|Age|City");
        columnAligner.AddRow("John Doe|25|New York");

        // Act
        columnAligner.Flush(Logger);
        columnAligner.Flush(Logger);
        var actualOutput = stringBuilder.ToString();

        var expectedOutput = @"Name     Age City
John Doe 25  New York
";

        // Assert
        Assert.Equal(expectedOutput, actualOutput);
    }
}

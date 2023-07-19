using System.Text;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.Console;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.Console;

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
        
        var expectedOutput = $"Name       Age City{Environment.NewLine}" +
                                  $"John Doe   25  New York{Environment.NewLine}" +
                                  $"Jane Smith 30  Los Angeles{Environment.NewLine}";

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
        
        var expectedOutput = $"Name        Age  City{Environment.NewLine}" +
                                  $"John Doe    25   New York{Environment.NewLine}" +
                                  $"Jane Smith  30   Los Angeles{Environment.NewLine}";

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

        var expectedOutput = $"Name     Age City{Environment.NewLine}" +
                             $"John Doe 25  New York{Environment.NewLine}";

         // Assert
         Assert.Equal(expectedOutput, actualOutput);
    }
}

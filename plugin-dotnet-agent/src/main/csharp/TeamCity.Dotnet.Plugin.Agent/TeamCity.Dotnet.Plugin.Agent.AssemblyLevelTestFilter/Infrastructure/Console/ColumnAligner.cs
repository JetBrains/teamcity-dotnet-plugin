using System.Text;

namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure.Console;

internal class ColumnAligner
{
    private readonly List<int> _columnWidths = new();
    private readonly List<string[]> _rows = new();
    private readonly char _separator;
    private readonly int _minPadding;

    public ColumnAligner(char separator, int minPadding = 1)
    {
        _separator = separator;
        _minPadding = minPadding;
    }

    public void AddRow(string input)
    {
        var columns = input.Split(_separator);
        UpdateMaxColumnWidths(columns);
        _rows.Add(columns);
    }

    public void Flush(Action<string> logger)
    {
        foreach (var row in _rows)
        {
            var alignedColumns = new StringBuilder();

            for (int i = 0; i < row.Length; i++)
            {
                alignedColumns.Append(row[i].PadRight(_columnWidths[i] + _minPadding));
            }

            logger(alignedColumns.ToString());
        }

        _rows.Clear();
    }

    private void UpdateMaxColumnWidths(string[] columns)
    {
        for (var i = 0; i < columns.Length; i++)
        {
            var columnWidth = columns[i].Length;

            if (i >= _columnWidths.Count)
            {
                _columnWidths.Add(columnWidth);
            }
            else
            {
                _columnWidths[i] = Math.Max(_columnWidths[i], columnWidth);
            }
        }
    }
}


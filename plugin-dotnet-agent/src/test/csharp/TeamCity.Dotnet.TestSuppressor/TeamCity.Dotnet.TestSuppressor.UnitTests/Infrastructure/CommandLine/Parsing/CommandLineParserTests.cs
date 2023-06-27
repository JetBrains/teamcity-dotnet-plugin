using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Parsing;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.CommandLine.Parsing;

public class CommandLineParserTests
{
    private readonly CommandLineParser<TestCommand> _parser = new();
    
    [Fact]
    public void Parse_EmptyArgs_OnlyBasicCommandIsActive()
    {
        // arrange
        var args = Enumerable.Empty<string>();

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(1, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_RootLevelHelpFlag_RootLevelHelpFlagIsSet()
    {
        // arrange
        var args = new [] { "-h" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Help", "true");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_RootLevelVerbosityArgumentWithoutValue_RootLevelVerbosityValueIsNotSet()
    {
        // arrange
        var args = new [] { "-v" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(1, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_RootLevelVerbosityArgumentWithValue_RootLevelVerbosityValueIsSet()
    {
        // arrange
        var args = new [] { "-v", "detailed" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Verbosity", "detailed");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_RequiredFlagArgument_Recognized()
    {
        // arrange
        var args = new [] { "--flag" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Flag", "true");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_StringArgument_Recognized()
    {
        // arrange
        var args = new [] { "--string" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:String", "true");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_StringArgumentWithValue_Recognized()
    {
        // arrange
        var args = new [] { "--req-string", "VALUE" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:RequiringValueString", "VALUE");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_StringArgumentWithoutValue_NotRecognized()
    {
        // arrange
        var args = new [] { "--req-string" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(1, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Theory]
    [InlineData(nameof(TestEnum.Abc), TestEnum.Abc)]
    [InlineData(nameof(TestEnum.Bca), TestEnum.Bca)]
    [InlineData(nameof(TestEnum.Cab), TestEnum.Cab)]
    public void Parse_ValidEnumValue_Recognized(string str, TestEnum val)
    {
        // arrange
        var args = new [] { "--enum", str };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Enum", val.ToString());
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_UnknownArgument_RecognizedAsUnknownArgument()
    {
        // arrange
        var args = new [] { "UNKNOWN" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(1, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        Assert.Equal(1, result.UnknownParameters.Count);
        Assert.Contains(result.UnknownParameters, a => a == "UNKNOWN");
    }
    
    [Fact]
    public void Parse_CommandArgument_RecognizedNestedCommand()
    {
        // arrange
        var args = new [] { "aaa" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:IsActive", "true");
        Assert.Empty(result.UnknownParameters);
    }
    
    [Fact]
    public void Parse_CommandArgumentWithHelpOption_RecognizedNestedCommandWithHelpOption()
    {
        // arrange
        var args = new [] { "aaa", "--help" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(3, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:Help", "true");
    }
    
    [Fact]
    public void Parse_CommandArgumentWithVerbosityOption_RecognizedNestedCommandWithVerbosityOption()
    {
        // arrange
        var args = new [] { "aaa", "--verbosity", "quiet" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(4, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:Verbosity", "quiet");
        AssertContainsKv(result.Mappings, "TestCommand:Verbosity", "quiet");
    }
    
    [Fact]
    public void Parse_CommandArgumentWithOption_RecognizedNestedCommandWithOption()
    {
        // arrange
        var args = new [] { "aaa", "--nested-option", "VALUE" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(3, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:NestedOption", "VALUE");
    }
    
    [Fact]
    public void Parse_TwoCommandsArgument_RecognizedFirstNestedCommandAndSecondRecognizedAsUnknown()
    {
        // arrange
        var args = new [] { "aaa", "bbb" };

        // act
        var result = _parser.Parse(args); 

        // assert
        Assert.Equal(2, result.Mappings.Count);
        AssertContainsKv(result.Mappings, "TestCommand:IsActive", "true");
        AssertContainsKv(result.Mappings, "TestCommand:Aaa:IsActive", "true");
        Assert.Equal(1, result.UnknownParameters.Count);
        Assert.Contains(result.UnknownParameters, a => a == "bbb");
    }
    
    private static void AssertContainsKv(IDictionary<string, string> dictionary, string key, string value) =>
        Assert.True(dictionary.ContainsKey(key) && dictionary[key] == value);

    private class TestCommand : Command
    {
        [Command("aaa")]
        public AaaCommand? Aaa { get; set; }
        
        [Command("bbb")]
        public BbbCommand? Bbb { get; set; }
        
        [CommandOption(false, "--flag", "-f")]
        public bool Flag { get; set; }

        [CommandOption(false, "--string", "-s")]
        public string String { get; set; } = "";

        [CommandOption(true, "--req-string", "-rs")]
        public string RequiringValueString { get; set; } = "";
        
        [CommandOption(true, "--enum", "-e")]
        public TestEnum Enum { get; set; }
    }
    
    private class AaaCommand : Command
    {
        [CommandOption(true, "--nested-option", "-no")]
        public string NestedOption { get; set; } = "";
    }
    
    private class BbbCommand : Command {}
    
    public enum TestEnum { Abc = 0, Bca, Cab }
}

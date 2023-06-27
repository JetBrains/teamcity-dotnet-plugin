using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Moq;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Commands;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Help;
using TeamCity.Dotnet.TestSuppressor.Infrastructure.CommandLine.Validation;

namespace TeamCity.Dotnet.TestSuppressor.UnitTests.Infrastructure.CommandLine;

public class CommandRouterTests
{
    private readonly Mock<IOptions<Command>> _mockOptions;
    private readonly Mock<ICommandValidator> _mockValidator;
    private readonly Mock<IHelpPrinter> _mockHelpPrinter;
    private readonly Mock<ICommandHandler<NestedCommand1>> _mockNestedCommand1Handler;
    private readonly Mock<ILogger<CommandRouter<Command>>> _mockLogger;
    private readonly Mock<IHostApplicationLifetime> _mockApplicationLifetime;
    private CommandRouter<Command> _router;

    public CommandRouterTests()
    {
        _mockOptions = new Mock<IOptions<Command>>();
        _mockValidator = new Mock<ICommandValidator>();
        _mockHelpPrinter = new Mock<IHelpPrinter>();
        _mockNestedCommand1Handler = new Mock<ICommandHandler<NestedCommand1>>();
        _mockLogger = new Mock<ILogger<CommandRouter<Command>>>();
        _mockApplicationLifetime = new Mock<IHostApplicationLifetime>();

        _router = new CommandRouter<Command>(
            _mockOptions.Object,
            _mockValidator.Object,
            _mockHelpPrinter.Object,
            new[] { _mockNestedCommand1Handler.Object },
            _mockLogger.Object,
            _mockApplicationLifetime.Object
        );
    }

    [Fact]
    public async Task StartAsync_WithValidCommandAndHandler_ShouldExecuteHandler()
    {
        // Arrange
        var command = new TestCommand();
        var subCommand = new NestedCommand1 { IsActive = true };
        command.Nested1 = subCommand;
        _mockOptions.Setup(o => o.Value).Returns(command);
        _mockValidator.Setup(v => v.Validate(command)).Returns(ValidationResult.Valid);

        // Act
        await _router.Route();

        // Assert
        _mockNestedCommand1Handler.Verify(h => h.ExecuteAsync(subCommand), Times.Once);
    }

    [Fact]
    public async Task StartAsync_WithValidCommandButInactiveSubCommand_ShouldPrintHelpAndStopApplication()
    {
        // Arrange
        var command = new TestCommand { Nested1 = new NestedCommand1() };
        _mockOptions.Setup(o => o.Value).Returns(command);
        _mockValidator.Setup(v => v.Validate(command)).Returns(ValidationResult.Valid);

        // Act
        await _router.Route();

        // Assert
        _mockHelpPrinter.Verify(h => h.PrintHelp(It.IsAny<Command>()), Times.Once);
        _mockApplicationLifetime.Verify(a => a.StopApplication(), Times.Once);
    }

    [Fact]
    public async Task StartAsync_WithHelpRequested_ShouldPrintHelpAndStopApplication()
    {
        // Arrange
        var command = new TestCommand { Help = true };
        _mockOptions.Setup(o => o.Value).Returns(command);

        // Act
        await _router.Route();

        // Assert
        _mockHelpPrinter.Verify(h => h.PrintHelp(It.IsAny<Command>()), Times.Once);
        _mockApplicationLifetime.Verify(a => a.StopApplication(), Times.Once);
    }

    [Fact]
    public async Task StartAsync_WithSubCommandHelpRequested_ShouldPrintHelpAndStopApplication()
    {
        // Arrange
        var command = new TestCommand { Nested1 = new NestedCommand1 { Help = true } };
        _mockOptions.Setup(o => o.Value).Returns(command);
        _mockValidator.Setup(v => v.Validate(command)).Returns(ValidationResult.Invalid("Invalid Command"));

        // Act
        await _router.Route();

        // Assert
        _mockHelpPrinter.Verify(h => h.PrintHelp(It.IsAny<Command>()), Times.Once);
        _mockApplicationLifetime.Verify(a => a.StopApplication(), Times.Once);
    }

    [Fact]
    public async Task StartAsync_WithInvalidCommand_ShouldPrintHelpAndStopApplication()
    {
        // Arrange
        var command = new TestCommand();
        _mockOptions.Setup(o => o.Value).Returns(command);
        _mockValidator.Setup(v => v.Validate(command)).Returns(ValidationResult.Invalid("Invalid Command"));

        // Act
        await _router.Route();

        // Assert
        _mockHelpPrinter.Verify(h => h.PrintHelp(command), Times.Once);
        _mockApplicationLifetime.Verify(a => a.StopApplication(), Times.Once);
    }

    [Fact]
    public async Task StartAsync_WithNoActiveSubcommand_ShouldPrintHelpAndStopApplication()
    {
        // Arrange
        var command = new TestCommand();
        _mockValidator.Setup(v => v.Validate(command)).Returns(ValidationResult.Invalid("Invalid Command"));
        _mockOptions.Setup(o => o.Value).Returns(command);

        // Act
        await _router.Route();

        // Assert
        _mockHelpPrinter.Verify(h => h.PrintHelp(It.IsAny<Command>()), Times.Once);
        _mockApplicationLifetime.Verify(a => a.StopApplication(), Times.Once);
    }

    public class NestedCommand1Handler : ICommandHandler<NestedCommand1>
    {
        public Task ExecuteAsync(NestedCommand1 command)
        {
            throw new NotImplementedException();
        }
    }

    public class TestCommand : Command
    {
        [Command("nested1")]
        public NestedCommand1? Nested1 { get; set; }
        
        [Command("nested2")]
        public NestedCommand2? Nested2 { get; set; }
    }
    
    public class NestedCommand1 : Command {}
    
    public class NestedCommand2 : Command {}
}

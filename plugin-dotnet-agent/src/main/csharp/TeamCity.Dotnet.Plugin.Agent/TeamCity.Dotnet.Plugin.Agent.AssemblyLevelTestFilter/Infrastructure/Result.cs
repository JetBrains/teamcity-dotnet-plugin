namespace TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.Infrastructure;

public abstract class Result<TSuccess, TError>
    where TError : Exception
{
    public abstract bool IsSuccess { get; }
    
    public abstract bool IsError { get; }
    
    public abstract TSuccess Value { get; }
    
    public abstract TError Exception { get; }

    private Result() {}
    
    public static Result<TSuccess, TError> Success(TSuccess value) => new SuccessResult(value);
    
    public static Result<TSuccess, TError> Error(TError value) => new ErrorResult(value);

    private sealed class SuccessResult : Result<TSuccess, TError>
    {
        public SuccessResult(TSuccess value)
        {
            this.Value = value;
        }

        public override bool IsSuccess => true;
        
        public override bool IsError => false;
        
        public override TSuccess Value { get; }

        public override TError Exception => throw new InvalidOperationException("Attempt to get Error value from Success");
    }

    private sealed class ErrorResult : Result<TSuccess, TError>
    {
        public ErrorResult(TError value)
        {
            Exception = value;
        }

        public override bool IsSuccess => false;
        
        public override bool IsError => true;
        
        public override TSuccess Value => throw new InvalidOperationException("Attempt to get Success value from Error");
        
        public override TError Exception { get; }
    }
}

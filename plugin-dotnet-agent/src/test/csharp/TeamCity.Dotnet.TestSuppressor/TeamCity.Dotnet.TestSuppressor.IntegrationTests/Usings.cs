global using Xunit;

#if DISABLE_TEST_PARALLELIZATION
[assembly: CollectionBehavior(DisableTestParallelization = true)]
#endif
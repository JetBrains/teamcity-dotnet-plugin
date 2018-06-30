package jetbrains.buildServer.rx

interface Subject<T> : Observable<T>, Observer<T> {}
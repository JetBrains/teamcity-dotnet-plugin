package jetbrains.buildServer.rx

data class NotificationError<T>(val error: Exception) : Notification<T>(NotificationKind.OnError)
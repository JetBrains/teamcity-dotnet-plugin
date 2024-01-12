

package jetbrains.buildServer.rx

data class NotificationNext<T>(val value: T) : Notification<T>(NotificationKind.OnNext)
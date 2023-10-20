
import ratelimit.{RateLimitConfig, RateLimitable, RateLimiter, RateLimited}

import java.time.Duration
import java.util.UUID
import scala.util.Try

enum NotificationKind:
  case Status
  case News
  case Marketing

case class Notification(customerId: UUID, kind: NotificationKind, body: String)

object Notification {
  given RateLimitable[Notification] with
    import NotificationKind._

    private val notificationKindToRateLimit =
      Map(
        Status -> (3, Duration.ofMinutes(1)),
        News -> (1, Duration.ofDays(1)),
        Marketing -> (1, Duration.ofHours(1)))

    def config(notification: Notification): RateLimitConfig =
      val (rate, period) = notificationKindToRateLimit(notification.kind)
      RateLimitConfig(s"${notification.kind}-${notification.customerId}", rate, period)
}

case class NotificationNotSent(notification: Notification, meta: Map[Any, Any] = Map.empty, cause: Option[Throwable] = None)
  extends Exception(s"($notification,$meta)", cause.orNull)

@FunctionalInterface trait NotificationService:
  def send(notification: Notification): Unit

class PrintNotificationService extends NotificationService:
  override def send(notification: Notification): Unit =
    println(s"✅Notification sent: $notification")

class RateLimitedNotificationService(delegate: NotificationService, rateLimiter: RateLimiter)(using RateLimitable[Notification]):
  def send(notification: Notification): Unit =
    rateLimiter(notification)(Try(delegate.send(notification)).toEither).flatten match
      case Right(result) => ()
      case Left(RateLimited(_, config)) =>
        throw NotificationNotSent(notification,
          meta = Map("reason" -> "rateLimited",
            "rateLimitConfig" -> config))
      case Left(e: Throwable) =>
        throw NotificationNotSent(notification, cause = Some(e))

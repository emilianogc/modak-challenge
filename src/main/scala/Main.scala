import ratelimit.{RateLimitConfig, RateLimitable, RateLimiter}

import java.time.Duration
import java.util.UUID

@main def main: Unit =
  given RateLimitable[Notification] with
    def config(notification: Notification) =
      RateLimitConfig(notification.customerId.toString, 3, Duration.ofSeconds(2))

  val notification = new RateLimitedNotificationService(new PrintNotificationService, RateLimiter.default)
  val customer1 = UUID.randomUUID()

  for i <- Range(0, 50) do
    Thread.sleep(200)
    try
      notification.send(Notification(customer1, NotificationKind.Status, s"Notification: $i)"))
    catch
      case e: Exception =>
        println(s"❌ $e")

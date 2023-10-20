import munit.FunSuite
import ratelimit.RateLimiter
import ratelimit.RateLimited

import java.util.UUID
import scala.collection.mutable

class RateLimitedNotificationServiceTest extends FunSuite {

  test("should not call deletage when rate limited") {
    val sent = mutable.Seq()
    val notificationService = new RateLimitedNotificationService(sent :+ _, RateLimiter.never)


    intercept[NotificationNotSent](notificationService.send(Notification(UUID.randomUUID(), NotificationKind.Status, "test")))
    assert(sent.isEmpty)
  }


}

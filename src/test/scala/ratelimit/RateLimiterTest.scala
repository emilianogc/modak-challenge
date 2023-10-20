package ratelimit

import munit.FunSuite

import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class RateLimiterTest extends FunSuite {
  test("rejections vs successes should match config") {
    case object Key
    given RateLimitable[Key.type] with
      def config(key: Key.type) =
        RateLimitConfig("key", 3, Duration.ofSeconds(2))

    val rateLimiter = RateLimiter.default
    val accepted, rejected = AtomicInteger(0)

    for i <- Range(0, 20) do
      Thread.sleep(200)
      rateLimiter(Key) {
        accepted.incrementAndGet()
      } match
        case Left(_) => rejected.incrementAndGet()
        case _ =>

    assertEquals(accepted.get(), 6)
    assertEquals(rejected.get(), 14)
  }
}

package ratelimit

import java.time.Duration
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

case class RateLimitConfig(key: String, amount: Int, period: Duration) {
  assert(amount > 0, s"Amount should be positive: $amount")
}

case class RateLimited(key: Any, config: RateLimitConfig)

trait RateLimitable[T]:
  def config(value: T): RateLimitConfig

object RateLimiter:
  def never[R] =
    new RateLimiter:
      override def apply[K: RateLimitable, R](key: K)(action: => R): Either[RateLimited, R] =
        Left(RateLimited(None, RateLimitConfig("None", Int.MaxValue, Duration.ZERO)))

  def default =
    new InMemoryScheduledRateLimiter(
      new ConcurrentHashMap(),
      new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory()))

@FunctionalInterface trait RateLimiter:
  def apply[K : RateLimitable, R](key: K)(action: => R): Either[RateLimited, R]

private class InMemoryScheduledRateLimiter(counters: ConcurrentMap[String, AtomicInteger], scheduler: ScheduledThreadPoolExecutor) extends RateLimiter:
  override def apply[K : RateLimitable, R](key: K)(action: => R): Either[RateLimited, R] =
    val config = summon[RateLimitable[K]].config(key)
    val counter = counters.computeIfAbsent(config.key, _ => new AtomicInteger(config.amount + 1))
    val currentValue = counter.decrementAndGet()
    if currentValue > 0 then
      if currentValue == config.amount then
        val reset: Runnable = () => counter.set(config.amount + 1)
        scheduler.schedule(reset, config.period.toMillis, TimeUnit.MILLISECONDS)
      Right(action)
    else
      Left(RateLimited(key, config))

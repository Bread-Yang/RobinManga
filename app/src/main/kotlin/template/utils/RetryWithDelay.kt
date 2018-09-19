package template.utils

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by Robin Yeung on 9/18/18.
 */
class RetryWithDelay(
        private val maxRetries: Int = 1,
        private val retryStrategy: (Int) -> Int = { 1000 },
        private val scheduler: Scheduler = Schedulers.computation()
) : Function<Observable<out Throwable>, Observable<*>> {

    private var retryCount = 0

    override fun apply(attempts: Observable<out Throwable>) = attempts.flatMap { error ->
        val count = ++retryCount
        if (count <= maxRetries) {
            Observable.timer(retryStrategy(count).toLong(), TimeUnit.MILLISECONDS, scheduler)
        } else {
            Observable.error(error as Throwable)
        }
    }
}
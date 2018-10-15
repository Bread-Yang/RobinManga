package template.ui.common.mvp

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import nucleus5.presenter.RxPresenter
import template.ui.common.mvp.transformer.DeliverLatestCacheFlowable

/**
 * Created by Robin Yeung on 8/21/18.
 */
open class BasePresenter<View> : RxPresenter<View>() {

    /**
     * Subscribes an observable with [deliverFirst] and adds it to the presenter's lifecycle
     * disposeble list.
     *
     * @param onNext function to execute when the observable emits an item.
     * @param onError function to execute when the observable throws an error.
     *
     */
    fun <T> Observable<T>.subscribeFirst(onError: ((View, Throwable) -> Unit)? = null, onNext: (View, T) -> Unit) =
            compose(deliverFirst<T>()).subscribe(split(onNext, onError)).apply { add(this) }

    /**
     * Subscribes an observable with [deliverLatestCache] and adds it to the presenter's lifecycle
     * disposeble list.
     *
     * @param onNext function to execute when the observable emits an item.
     * @param onError function to execute when the observable throws an error.
     *
     */
    fun <T> Observable<T>.subscribeLatestCache(onError: ((View, Throwable) -> Unit)? = null, onNext: (View, T) -> Unit) =
            compose(deliverLatestCache<T>()).subscribe(split(onNext, onError)).apply { add(this) }

    /**
     * Subscribes an flowable with [DeliverLatestCacheFlowable] and adds it to the presenter's lifecycle
     * disposeble list.
     *
     * @param onNext function to execute when the flowable emits an item.
     * @param onError function to execute when the flowable throws an error.
     *
     */
    fun <T> Flowable<T>.subscribeLatestCache(onError: ((View, Throwable) -> Unit)? = null, onNext: (View, T) -> Unit) =
            compose(DeliverLatestCacheFlowable<View, T>(view().toFlowable(BackpressureStrategy.BUFFER)))
                    .subscribe(split(onNext, onError))
                    .apply {
                        add(this)
                    }

    /**
     * Subscribes an observable with [deliverReplay] and adds it to the presenter's lifecycle
     * disposeble list.
     *
     * @param onNext function to execute when the observable emits an item.
     * @param onError function to execute when the observable throws an error.
     *
     */
    fun <T> Observable<T>.subscribeReplay(onError: ((View, Throwable) -> Unit)? = null, onNext: (View, T) -> Unit) =
            compose(deliverReplay<T>()).subscribe(split(onNext, onError)).apply { add(this) }
}
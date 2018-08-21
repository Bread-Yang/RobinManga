package template.ui.common.mvp

import io.reactivex.Observable
import nucleus5.presenter.RxPresenter

/**
 * Created by Robin Yeung on 8/21/18.
 */
class BasePresenter<View> : RxPresenter<View>() {

    /**
     * Subscribes an observable with [deliverFirst] and adds it to the presenter's lifecycle
     * disposeble list.
     *
     * @param onNext function to execute when the observable emits an item.
     * @param onError function to execute when the observable throws an error.
     *
     */
    fun <T> Observable<T>.subscribeFirst(onNext: (View, T) -> Unit, onError: ((View, Throwable) -> Unit)? = null) =
            compose(deliverFirst<T>()).subscribe(split(onNext, onError)).apply { add(this) }

    /**
     * Subscribes an observable with [deliverLatestCache] and adds it to the presenter's lifecycle
     * disposeble list.
     *
     * @param onNext function to execute when the observable emits an item.
     * @param onError function to execute when the observable throws an error.
     *
     */
    fun <T> Observable<T>.subscribeLatestCache(onNext: (View, T) -> Unit, onError: ((View, Throwable) -> Unit)? = null) =
            compose(deliverLatestCache<T>()).subscribe(split(onNext, onError)).apply { add(this) }

    /**
     * Subscribes an observable with [deliverReplay] and adds it to the presenter's lifecycle
     * disposeble list.
     *
     * @param onNext function to execute when the observable emits an item.
     * @param onError function to execute when the observable throws an error.
     *
     */
    fun <T> Observable<T>.subscribeReplay(onNext: (View, T) -> Unit, onError: ((View, Throwable) -> Unit)? = null) =
            compose(deliverReplay<T>()).subscribe(split(onNext, onError)).apply { add(this) }
}
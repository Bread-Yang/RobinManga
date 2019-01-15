package template.ui.common.mvp.transformer

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Notification
import io.reactivex.functions.BiFunction
import nucleus5.presenter.delivery.Delivery
import nucleus5.view.OptionalView
import org.reactivestreams.Publisher

/**
 * Created by Robin Yeung on 9/25/18.
 */
class DeliverLatestCacheFlowable<View, T>(private val view: Flowable<OptionalView<View>>)
    : FlowableTransformer<T, Delivery<View, T>> {


    override fun apply(flowable: Flowable<T>): Publisher<Delivery<View, T>> {
        return Flowable
                .combineLatest(
                        view,
                        flowable
                                .materialize()
                                .filter {
                                    !it.isOnComplete
                                },
                        BiFunction<OptionalView<View>, Notification<T>, Array<Any>> { view, notification ->
                            arrayOf(view, notification)
                        }
                )
                .concatMap { pack ->
                    NucleusUtils.validateFlowable(pack[0] as OptionalView<View>, pack[1] as Notification<T>)
                }
    }
}
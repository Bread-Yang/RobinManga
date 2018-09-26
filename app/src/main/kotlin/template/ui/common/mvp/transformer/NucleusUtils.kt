package template.ui.common.mvp.transformer

import io.reactivex.Flowable
import io.reactivex.Notification
import nucleus5.presenter.delivery.Delivery
import nucleus5.view.OptionalView

/**
 * Created by Robin Yeung on 9/25/18.
 */
object NucleusUtils {

    fun <View, T> valideFlowable(view: OptionalView<View>, notification: Notification<T>)
            : Flowable<Delivery<View, T>> {
        return if (Delivery.isValid(view, notification))
            Flowable.just(Delivery<View, T>(view.view, notification))
        else
            Flowable.empty()
    }
}
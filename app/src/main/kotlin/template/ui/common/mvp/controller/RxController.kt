package template.ui.common.mvp.controller

import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Robin Yeung on 8/22/18.
 */
abstract class RxController(bundle: Bundle? = null) : BaseController(bundle) {

    var untilDetachDisposibles = CompositeDisposable()
        private set

    var untilDestoryDisposibles = CompositeDisposable()
        private set

    @CallSuper
    override fun onAttach(view: View) {
        super.onAttach(view)
        if (untilDetachDisposibles.isDisposed) {
            untilDetachDisposibles = CompositeDisposable()
        }
    }

    @CallSuper
    override fun onDetach(view: View) {
        super.onDetach(view)
        untilDetachDisposibles.dispose()
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        if (untilDestoryDisposibles.isDisposed) {
            untilDestoryDisposibles = CompositeDisposable()
        }
        return super.onCreateView(inflater, container, savedViewState)
    }

    @CallSuper
    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        untilDestoryDisposibles.dispose()
    }

    fun <T> Observable<T>.subscribeUntilDetach(): Disposable {
        return subscribe().also {
            untilDetachDisposibles.add(it)
        }
    }

    fun <T> Observable<T>.subscribeUntilDetach(onNext: (T) -> Unit): Disposable {
        return subscribe(onNext).also {
            untilDetachDisposibles.add(it)
        }
    }

    fun <T> Observable<T>.subscribeUntilDetach(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
        return subscribe(onNext, onError).also {
            untilDetachDisposibles.add(it)
        }
    }

    fun <T> Observable<T>.subscribeUntilDestroy(): Disposable {
        return subscribe().also {
            untilDestoryDisposibles.add(it)
        }
    }

    fun <T> Observable<T>.subscribeUntilDestroy(onNext: (T) -> Unit): Disposable {
        return subscribe(onNext).also {
            untilDestoryDisposibles.add(it)
        }
    }

    fun <T> Observable<T>.subscribeUntilDestroy(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable {
        return subscribe(onNext, onError).also {
            untilDestoryDisposibles.add(it)
        }
    }
}
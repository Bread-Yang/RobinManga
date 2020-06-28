package template.ui.common.mvp.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Robin Yeung on 9/11/18.
 */
abstract class RxActivity : AppCompatActivity() {

    var untilDestoryDisposibles = CompositeDisposable()
        private set

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (untilDestoryDisposibles.isDisposed) {
            untilDestoryDisposibles = CompositeDisposable()
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        untilDestoryDisposibles.dispose()
    }

    fun add(disposable: Disposable) {
        untilDestoryDisposibles.add(disposable)
    }

    fun remove(disposable: Disposable) {
        untilDestoryDisposibles.remove(disposable)
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
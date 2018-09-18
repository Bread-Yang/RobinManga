package template.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable?.isNullOrDisposed() = this == null || isDisposed

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}
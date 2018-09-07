package template.extensions

import io.reactivex.disposables.Disposable

fun Disposable?.isNullOrDisposed() = this == null || isDisposed
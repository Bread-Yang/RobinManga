package template.extensions

import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import template.network.ProgressListener
import template.network.ProgressResponseBody

/**
 * Converts the call to an observable which will emit the response, then complete
 */
fun Call.asObservable(): Observable<Response> {
    return CallObservable(this)
}

fun Call.asObservableSuccess() : Observable<Response> {
    return asObservable().doOnNext {
        if (!it.isSuccessful) {
            it.close()
            throw Exception("HTTP error ${it.code()}")
        }
    }
}

/**
 * Converts the call to a flowable which will emit the latest response, then complete
 */
fun Call.asFlowable(): Flowable<Response> {
    return asObservable().toFlowable(BackpressureStrategy.LATEST)
}

fun Call.asFlowableSuccess() : Flowable<Response> {
    return asFlowable().doOnNext {
        if (!it.isSuccessful) {
            it.close()
            throw Exception("HTTP error ${it.code()}")
        }
    }
}

fun OkHttpClient.newCallWithProgress(request: Request, listener: ProgressListener): Call {
    val progressClient = newBuilder()
            .cache(null)
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                        .body(ProgressResponseBody(originalResponse.body()!!, listener))
                        .build()
            }
            .build()

    return progressClient.newCall(request)
}

/**
 * Converts the call to a single which will emit the response or an error
 */
fun Call.asSingle(): Single<Response> {
    return asObservable().singleOrError()
}

internal class CallObservable(private val originalCall: Call) : Observable<Response>() {

    override fun subscribeActual(observer: Observer<in Response>) {
        // Since Call is a one-shot type, clone it for each new observer.
        val call = originalCall.clone()
        observer.onSubscribe(CallDisposable(call))

        var terminated = false
        try {
            val response = call.execute()
            if (!call.isCanceled) {
                observer.onNext(response)
            }
            if (!call.isCanceled) {
                terminated = true
                observer.onComplete()
            }
        } catch (t: Throwable) {
            Exceptions.throwIfFatal(t)
            if (terminated) {
                RxJavaPlugins.onError(t)
            } else if (!call.isCanceled) {
                try {
                    observer.onError(t)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(t, inner))
                }

            }
        }
    }
}

private class CallDisposable internal constructor(private val call: Call) : Disposable {

    override fun dispose() {
        call.cancel()
    }

    override fun isDisposed(): Boolean {
        return call.isCanceled
    }
}
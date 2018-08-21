package template.ui.main

import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import template.api.Api
import template.ui.common.mvp.BasePresenter
import template.ui.main.cell.MainModel
import timber.log.Timber
import javax.inject.Inject

class MainPresenter : BasePresenter<MainController>() {

    @Inject
    lateinit var api: Api

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        fetchHistory()
    }

    fun fetchHistory() {
        api.getHistoricalPrice()
                .doOnNext {
                    Timber.e("得到的数据是")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toObservable()
                .subscribeLatestCache(
                        { view, throwable ->
                            view.onError(throwable)
                        },
                        { view, historical ->
                            view.onHistoricalLoaded(
                                    historical.bpi.toList().asReversed()
                                            .map {
                                                MainModel(it.first, it.second)
                                            }
                            )
                        }
                )
    }
}
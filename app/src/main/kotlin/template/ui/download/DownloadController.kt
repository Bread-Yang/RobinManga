package template.ui.download

import android.view.View
import io.reactivex.disposables.Disposable
import nucleus5.factory.RequiresPresenter
import template.R
import template.data.download.model.Download
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusDaggerController

/**
 * Controller that shows the currently active downloads.
 * Use R.layout.download_controller.
 */
@Layout(R.layout.download_controller)
@RequiresPresenter(DownloadPresenter::class)
class DownloadController : NucleusDaggerController<DownloadPresenter>() {

    /**
     * Adapter containing th active downloads.
     */
    private var adapter: DownloadAdapter? = null

    /**
     * Map of disposables for active downloads.
     */
    private val progressDisposables by lazy {
        HashMap<Download, Disposable>()
    }

    /**
     * Whether the download queue is running or not.
     */
    private var isRunning: Boolean = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View) {

    }

    override fun initPresenterOnce() {
    }

    private fun setInformationView() {
        if (presenter.downloadQueue.isEmpty()) {

        }
    }
}

package template.ui.download

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.download_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.data.download.DownloadService
import template.data.download.model.Download
import template.ui.common.annotation.Layout
import template.ui.common.mvp.controller.NucleusDaggerController
import java.util.concurrent.TimeUnit

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
        // Check if download queue is empty and update information accordingly.
        setInformationView()

        // Initialize adapter.
        adapter = DownloadAdapter()
        recyclerView.adapter = adapter

        // Set the layout manager for the recycler and fixed size.
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)

        // Suscribe to changes
        DownloadService.runningSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy {
                    onQueueStatusChange(it)
                }

        presenter.getDownloadStatusFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy {
                    onStatusChange(it)
                }
    }

    override fun initPresenterOnce() {
    }

    /**
     * Called when the queue's status has changed, Updates the visibility of the buttons.
     *
     * @param running whether the queue is now running or not.
     */
    private fun onQueueStatusChange(running: Boolean) {
        isRunning = running
        activity?.invalidateOptionsMenu()

        // Check if download queue is empty and update information accordingly.
        setInformationView()
    }

    /**
     * Called when the status of a download changes.
     *
     * @param download the download whose status has changed.
     */
    private fun onStatusChange(download: Download) {
        when (download.status) {
            Download.DOWNLOADING -> {
                observeProgress(download)
            }
        }
    }

    /**
     * Observe the progress of a download and notify the view.
     *
     * @param download the download to observe its progress.
     */
    private fun observeProgress(download: Download) {
        val disposable = Flowable.interval(50, TimeUnit.MILLISECONDS)
                // Get the sum of percentages for all the pages.
                .flatMap {
                    Flowable.fromIterable(download.pages)
                            .map {
                                it.progress
                            }
                            .reduce { x, y -> x + y }
                            .toFlowable()
                }
                // keep only the lastest emission to avoid backpressure.
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { progress ->
                    // Update the view only if the progress has changed.
                    if (download.totalProgress != progress) {
                        download.totalProgress = progress
                        onUpdateProgress(download)
                    }
                }

        // Avoid leaking disposables
        progressDisposables.remove(download)?.dispose()

        progressDisposables.put(download, disposable)
    }

    /**
     * Called when the progress of a download changes.
     *
     * @param download the download whose progress has changed.
     */
    fun onUpdateProgress(download: Download) {
        getHolder(download)?.notifyProgress()
    }

    /**
     * Returns the holder for the given download.
     *
     * @param download the download to find.
     * @return the holder of the download or null if it's not bound.
     */
    private fun getHolder(download: Download): DownloadHolder? {
        return recyclerView?.findViewHolderForItemId(download.chapter.id!!) as? DownloadHolder
    }

    /**
     * Set information view when queue is empty
     */
    private fun setInformationView() {
        if (presenter.downloadQueue.isEmpty()) {
            empty_view?.show(R.drawable.ic_file_download_black_128dp, R.string.information_no_downloads)
        } else {
            empty_view?.hide()
        }
    }

    /**
     * Called from the presenter to assign the downloads for the adapter.
     *
     * @param downloads the downloads from the queue.
     */
    fun onNextDownloads(downloads: List<Download>) {
        activity?.invalidateOptionsMenu()
        setInformationView()
        adapter?.setItems(downloads)
    }
}

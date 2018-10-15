package template.ui.download

import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.download_controller.*
import nucleus5.factory.RequiresPresenter
import template.R
import template.annotation.Layout
import template.data.download.DownloadService
import template.data.download.model.Download
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
     * Adapter containing the active downloads.
     */
    private var adapter: DownloadAdapter? = null

    /**
     * Map of disposables for active downloads.
     */
    private val progressDisposablesMap by lazy {
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
        setEmptyInformationView()

        // Initialize adapter.
        adapter = DownloadAdapter()
        recyclerView.adapter = adapter

        // Set the layout manager for the recycler and fixed size.
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.setHasFixedSize(true)

        // Subscribe to changes
        DownloadService.runningSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy {
                    onQueueStatusChange(it)
                }

        presenter.getSingleDownloadStatusFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy {
                    onSingleDownloadStatusChange(it)
                }

        presenter.getDownloadProgressFlowable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeUntilDestroy {
                    onUpdateAdapterItemDownloadedPages(it)
                }
    }

    override fun initPresenterOnce() {
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.download_queue, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // Set start button visibility.
        menu.findItem(R.id.start_queue).isVisible = !isRunning && !presenter.downloadQueue.isEmpty()

        // Set pause button visibility.
        menu.findItem(R.id.pause_queue).isVisible = isRunning

        // Set clear button visibility.
        menu.findItem(R.id.clear_queue).isVisible = !presenter.downloadQueue.isEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val context = applicationContext ?: return false
        when (item.itemId) {
            R.id.start_queue -> DownloadService.start(context)
            R.id.pause_queue -> {
                DownloadService.stop(context)
                presenter.pauseDownloads()
            }
            R.id.clear_queue -> {
                DownloadService.stop(context)
                presenter.clearQueue()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onDestroyView(view: View) {
        for (disposable in progressDisposablesMap.values) {
            disposable.dispose()
        }
        progressDisposablesMap.clear()
        adapter = null

        super.onDestroyView(view)
    }

    /**
     * Dispose the given download from the progress disposables.
     *
     * @param download the download to dispose.
     */
    private fun disposeProgress(download: Download) {
        progressDisposablesMap.remove(download)?.dispose()
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
        setEmptyInformationView()
    }

    /**
     * Called when the status of a download changes.
     *
     * @param download the download whose status has changed.
     */
    private fun onSingleDownloadStatusChange(download: Download) {
        when (download.status) {
            Download.DOWNLOADING -> {
                observeSingleDownloadProgress(download)
                // Initial update of the downloaded pages
                onUpdateAdapterItemDownloadedPages(download)
            }
            Download.DOWNLOADED -> {
                disposeProgress(download)
                onUpdateAdapterItemDownloadProgress(download)
                onUpdateAdapterItemDownloadedPages(download)
            }
            Download.ERROR -> disposeProgress(download)
        }
    }

    /**
     * Observe the progress of a download and notify the view.
     *
     * @param download the download to observe its progress.
     */
    private fun observeSingleDownloadProgress(download: Download) {
        val disposable = Flowable.interval(50, TimeUnit.MILLISECONDS)
                // Get the sum of percentages for all the pages.
                .flatMap {
                    Flowable.fromIterable(download.pages)
                            .map {
                                it.imageDownloadProgress
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
                        onUpdateAdapterItemDownloadProgress(download)
                    }
                }

        // Avoid leaking disposables
        progressDisposablesMap.remove(download)?.dispose()

        progressDisposablesMap.put(download, disposable)
    }

    /**
     * Called when the progress of a download changes.
     *
     * @param download the download whose progress has changed.
     */
    fun onUpdateAdapterItemDownloadProgress(download: Download) {
        getHolder(download)?.notifyDownloadProgress()
    }

    /**
     * Called when a page of a download is downloaded.
     *
     * @param download the download whose page has been downloaded.
     */
    fun onUpdateAdapterItemDownloadedPages(download: Download) {
        getHolder(download)?.notifyDownloadedPages()
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
    private fun setEmptyInformationView() {
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
    fun onDownloadListUpdate(downloads: List<Download>) {
        activity?.invalidateOptionsMenu()
        setEmptyInformationView()
        adapter?.setItems(downloads)
    }
}

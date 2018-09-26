package template.data.download

import android.content.Context
import android.webkit.MimeTypeMap
import com.hippo.unifile.UniFile
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.async
import okhttp3.Response
import template.App
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.data.download.model.Download
import template.data.download.model.DownloadQueue
import template.extensions.*
import template.source.SourceManager
import template.source.model.Page
import template.source.online.HttpSource
import template.utils.DiskUtil
import template.utils.RetryWithDelay
import timber.log.Timber

/**
 * This class is the one in charge of downloading chapters.
 *
 * Its [queue] contains the list of chapters
 */
class Downloader(
        private val context: Context,
        private val pathProvider: DownloadPathProvider,
        private val cache: DownloadCache
) {

    /**
     * Store for persisting downloads across restarts.
     */
    private val store = DownloadStore(context)

    /**
     * Queue where active downloads are kept.
     */
    val queue = DownloadQueue(store)

    /**
     * Source manager.
     */
    private val sourceManager: SourceManager = App.app.lazySourceManager.get()

    /**
     * Notifier for the downloader state and progress.
     */
    // TODO
//    private val notifier by lazy {
//        DownloadNotifier(context)
//    }

    /**
     * Downloader disposables.
     */
    private val disposables = CompositeDisposable()

    /**
     * Subject to send a list fo downloads to the downloader.
     */
    private val downloadProcessor = PublishProcessor.create<List<Download>>()

    /**
     * Subject to subscribe to the downloader status.
     */
    val runningSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    /**
     * Whether the downloader is running.
     */
    @Volatile
    private var isRunning: Boolean = false

    init {
        launchNow {
            val chapters = async {
                store.restore()
            }
            queue.addAll(chapters.await())
        }
    }

    /**
     * Starts the downloader. It doesn't do anything if it's already running or there isn't anything
     * to download.
     *
     * @return true if the downloader is started, false otherwise.
     */
    fun start(): Boolean {
        if (isRunning || queue.isEmpty()) {
            return false
        }

        if (disposables.size() == 0) {
            initializeDisposables()
        }

        val pending = queue.filter { it.status != Download.DOWNLOADED }
        pending.forEach {
            if (it.status != Download.QUEUE)
                it.status = Download.QUEUE
        }

        downloadProcessor.onNext(pending)
        return !pending.isEmpty()
    }

    /**
     * Stops the downloader.
     */
    fun stop(reason: String? = null) {
        destroyDisposables()
        queue
                .filter {
                    it.status == Download.DOWNLOADING
                }
                .forEach {
                    it.status = Download.ERROR
                }

        if (reason != null) {
            // TODO
//            notifier.onWarning(reason)
        } else {
            // TODO
//            if (notifier.paused) {
//                notifier.paused = false
//                notifier.onDownloadPaused()
//            } else if (notifier.isSingleChapter && !notifier.errorThrown) {
//                notifier.isSingleChapter = false
//            } else {
//                notifier.dismiss()
//            }
        }
    }

    /**
     * Pause the downloader
     */
    fun pause() {
        destroyDisposables()
        queue
                .filter {
                    it.status == Download.DOWNLOADING
                }
                .forEach {
                    it.status = Download.QUEUE
                }
        // TODO
//        notifier.paused = true
    }

    /**
     * Removes everything from the queue.
     *
     * @param isNotification value that determines if status is set (needed for view updateds)
     */
    fun clearQueue(isNotification: Boolean = false) {
        destroyDisposables()

        // Needed to update the chapter view.
        if (isNotification) {
            queue
                    .filter {
                        it.status == Download.QUEUE
                    }
                    .forEach {
                        it.status == Download.NOT_DOWNLOADED
                    }

            queue.clear()
//            notifier.dismiss()
        }
    }

    /**
     * Prepares the disposables to start downloading.
     */
    private fun initializeDisposables() {
        if (isRunning)
            return

        isRunning = true
        runningSubject.onNext(true)

        disposables.clear()

        disposables += downloadProcessor
                .concatMapIterable {
                    it
                }
                .concatMap {
                    downloadChapter(it)
                            .subscribeOn(Schedulers.io())
                }
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    completeDownload(it)
                }, { error ->
                    // TODO
//                    DownloadService.stop(context)
                    Timber.e(error)
//                    notifier.onError(error.message)
                })

    }

    /**
     * Destroys the downloader disposables.
     */
    private fun destroyDisposables() {
        if (!isRunning) return
        isRunning = false
        runningSubject.onNext(false)

        disposables.clear()
    }

    /**
     * Creates a download object for every chapter and adds them to the downloads queue.
     *
     * @param manga the manga of the chapters to download.
     * @param chapters the list of chapters to download.
     * @param autoStart whether to start the downloader after enqueuing the chapters.
     */
    fun queueChapters(manga: Manga, chapters: List<Chapter>, autoStart: Boolean) = launchUI {
        val source = sourceManager.get(manga.source) as? HttpSource ?: return@launchUI

        // Called in background thread, the operation can be slow with SAF.
        val chaptersWithoutDir = async {
            val mangaDir = pathProvider.findMangaDir(manga, source)

            chapters
                    // Avoid downloading chapters with the same name.
                    .distinctBy {
                        it.name
                    }
                    // Filter out those already download.
                    .filter {
                        mangaDir?.findFile(pathProvider.getChapterDirName(it))  == null
                    }
                    // Add chapters to queue from the start.
                    .sortedByDescending {
                        it.source_order
                    }
        }

        // Runs in mian thread (synchronization needed).
        val chaptersToQueue = chaptersWithoutDir.await()
                .filter { chapter ->
                    // Filter out those already enqueued.
                    queue.none {
                        it.chapter.id == chapter.id
                    }
                }
                // Create a download for each one.
                .map {
                    Download(source, manga, it)
                }

        if (chaptersToQueue.isNotEmpty()) {
            queue.addAll(chaptersToQueue)

            // Initialize queue size.
            // TODO
//            notifier.initialQueueSize = queue.size

            if (isRunning) {
                // Send the list of downloads to the downloader.
                downloadProcessor.onNext(chaptersToQueue)
            }

            // Start downloader if needed
            if (autoStart) {
//                DownloadService.start(this@Downloader.context)
            }
        }
    }

    /**
     * Returns the observable which downloads a chapter.
     *
     * @param download the chapter to be download.
     */
    private fun downloadChapter(download: Download): Flowable<Download> = Flowable.defer {
        val chapterDirname = pathProvider.getChapterDirName(download.chapter)
        val mangaDir = pathProvider.getMangaDir(download.manga, download.source)
        val tmpDir = mangaDir.createDirectory("${chapterDirname}_tmp")

        val pageListObservable =
                if (download.pages == null) {
                    // Pull page list from network and add them to download object
                    download.source
                            .fetchPageList(download.chapter)
                            .doOnNext {
                                if (it.isEmpty()) {
                                    throw Exception("Page list is empty")
                                }
                                download.pages = it
                            }

                } else {
                    // Or if the page list already exists, start from the file
                    Observable.just(download.pages)
                }

        pageListObservable
                .doOnNext {
                    // Delete all temporary (unfinished) files
                    tmpDir.listFiles()
                            ?.filter {
                                it.name!!.endsWith(".tmp")
                            }
                            ?.forEach {
                                it.delete()
                            }

                    download.downloadedImages = 0
                    download.status = Download.DOWNLOADING
                }
                // Get all the URLs to the source images, fetch pages if necessary
                .flatMap {
                    download.source.fetchAllImageUrlsFromPageList(it)
                }
                // Start downloading images, consider we can have downloaded images already
                .concatMap { page ->
                    getOrDownloadImage(page, download, tmpDir)
                }
                // Do when page is downloaded.
                .doOnNext {
                    // TODO
//                    notifier.onProgressChange(download)
                }
                .toList()
                .map {
                    download
                }
                // Do after download completes
                .doOnSuccess {
                    ensureSuccessfulDownload(download, mangaDir, tmpDir, chapterDirname)
                }
                // If the page list threw, it will resume here
                .onErrorReturn { error ->
                    download.status = Download.ERROR
//                    notifier.onError(error.message, download.chapter.name)
                    download
                }
                .toFlowable()
    }

    /**
     * Returns the observable which gets the image from the filesystem if it exists or downloads it
     * otherwise.
     *
     * @param page the page to download.
     * @param download the download of the page.
     * @param tmpDir the temporary directory of the download.
     */
    private fun getOrDownloadImage(page: Page, download: Download, tmpDir: UniFile): Observable<Page> {
        // If the image URL is empty, do nothing
        if (page.imageUrl == null)
            return Observable.just(page)

        val filename = String.format("%03d", page.number)       // 数字12,%03d出来就是: 012
        val tmpFile = tmpDir.findFile("$filename.tmp")

        // Delete temp file if it exists.
        tmpFile?.delete()

        // Try to find the image file.
        val imageFile =
                tmpDir.listFiles()!!
                        .find {
                            it.name!!.startsWith("$filename.")
                        }

        // If the image is already download. do nothing. Otherwise download from netrok
        val pageObservable =
                if (imageFile != null) {
                    Observable.just(imageFile)
                } else {
                    downloadImage(page, download.source, tmpDir, filename)
                }

        return pageObservable
                // when the image is ready, set image path, progress (just in case) and status
                .doOnNext { file ->
                    page.uri = file.uri
                    page.progress = 100
                    download.downloadedImages++
                    page.status = Page.READY
                }
                .map {
                    page
                }
                // Mark this page as error and allow to download the remaining
                .onErrorReturn {
                    page.progress = 0
                    page.status = Page.ERROR
                    page
                }
    }

    /**
     * Returns the observable which downloads the image from network.
     *
     * @param page the page to download.
     * @param source the source of the page.
     * @param tmpDir the temporary directory of the download.
     * @param filename the filename of the image.
     */
    private fun downloadImage(page: Page, source: HttpSource, tmpDir: UniFile, filename: String): Observable<UniFile> {
        page.status = Page.DOWNLOAD_IMAGE
        page.progress = 0
        return source.fetchImage(page)
                .map { response ->
                    val file = tmpDir.createFile("$filename.tmp")
                    try {
                        response.body()!!.source().saveTo(file.openOutputStream())
                        val extension = getImageExtensioin(response, file)
                        file.renameTo("$filename.$extension")
                    } catch (e: Exception) {
                        response.close()
                        file.delete()
                        throw e
                    }
                    file
                }
                // Retry 3 times, waiting 2, 4 and 8 seconds between attempts.
                .retryWhen(RetryWithDelay(3, { (2 shl it - 1) * 1000 }, Schedulers.trampoline()))
    }

    /**
     * Returns the extension of the downloaded image from the network response, or if it's null,
     * analyze the file. If everything fails, assume it's a jpg.
     *
     * @param response the network response of the image.
     * @param file the file where the image is already downloaded.
     */
    private fun getImageExtensioin(response: Response, file: UniFile): String {
        // Read content type if available.
        val mime = response.body()?.contentType()?.let {
            "${it.type()}/${it.subtype()}"
        }
                ?: context.contentResolver.getType(file.uri)    // Else guess from the uri.
                // Else read magic numbers.
                ?: DiskUtil.findImageMime {
                    file.openInputStream()
                }

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mime) ?: "jpg"
    }

    /**
     * Checks if the download was successful.
     *
     * @param download the download to check.
     * @param mangaDir the manga  directory of the download.
     * @param tmpDir the directory where the download is currently stored.
     * @param dirname the real (non temporary) directory name of the download.
     */
    private fun ensureSuccessfulDownload(download: Download, mangaDir: UniFile,
                                         tmpDir: UniFile, dirname: String) {
        // Ensure that the chapter folder has all the images.
        val downloadedImage = tmpDir.listFiles().orEmpty().filterNot {
            it.name!!.endsWith(".tmp")
        }

        download.status =
                if (downloadedImage.size == download.pages!!.size) {
                    Download.DOWNLOADED
                } else {
                    Download.ERROR
                }

        // Only rename the directory if it's downloaded.
        if (download.status == Download.DOWNLOADED) {
            tmpDir.renameTo(dirname)
            cache.addChapter(dirname, mangaDir, download.manga)
        }
    }

    /**
     * Completes a download. this method is called in the main thread.
     */
    private fun completeDownload(download: Download) {
        // Delete successful downloads from queue
        if (download.status == Download.DOWNLOADED) {
            // remove downloaded chapter from queue
            queue.remove(download)
        }
        if (areAllDownloadsFinished()) {

        }
    }

    /**
     * Returns true if all the queued downloads are in DOWNLOADED or ERROR state.
     */
    private fun areAllDownloadsFinished(): Boolean {
        return queue.none { it.status <= Download.DOWNLOADING }
    }
}


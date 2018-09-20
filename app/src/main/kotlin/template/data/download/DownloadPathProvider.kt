package template.data.download

import android.content.Context
import android.net.Uri
import com.hippo.unifile.UniFile
import template.App
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.extensions.getOrDefault
import template.source.Source
import template.utils.DiskUtil
import template.utils.preference.PreferencesHelper

/**
 * This class is used to provide the directories where the downloads should be saved.
 * It uses the following path scheme: /<root downloads dir>/<source name>/<manga>/<chapter>
 *
 * @param context the application context.
 */
class DownloadPathProvider(private val context: Context) {

    /**
     * Preferences helper.
     */
    private val preferences: PreferencesHelper = App.app.lazyPreferencesHelper.get()

    /**
     * The root directory for downloads.
     */
    private var downloadsDir = preferences.downloadsDirectory().getOrDefault().let {
        UniFile.fromUri(context, Uri.parse(it))
    }

    init {
        preferences.downloadsDirectory()
                .asObservable()
                .skip(1)
                .subscribe {
                    downloadsDir = UniFile.fromUri(context, Uri.parse(it))
                }
    }

    /**
     * Returns the download directory for a manga. For internal use only.
     *
     * @param manga the manga to query.
     * @param source the source of the manga.
     */
    internal fun getMangaDir(manga: Manga, source: Source): UniFile {
        return downloadsDir
                .createDirectory(getSourceDirName(source))
                .createDirectory(getMangaDirName(manga))
    }

    /**
     * Returns the download directory for a source if it exists.
     *
     * @param source the source to query.
     */
    fun findSourceDir(source: Source): UniFile? {
        return downloadsDir.findFile(getSourceDirName(source))
    }

    /**
     * Returns the download directory for a manga if it exists.
     *
     * @param manga the manga to query.
     * @param source the source of the manga.
     */
    fun findMangaDir(manga: Manga, source: Source): UniFile? {
        val sourceDir = findSourceDir(source)
        return sourceDir?.findFile(getMangaDirName(manga))
    }

    /**
     * Returns the download directory for a chapter if it exists.
     *
     * @param chapter the chapter to query.
     * @param manga the manga of the chapter.
     * @param source the source of the chapter.
     */
    fun findChapterDir(chapter: Chapter, manga: Manga, source: Source): UniFile? {
        val mangaDir = findMangaDir(manga, source)
        return mangaDir?.findFile(getChapterDirName(chapter))
    }

    /**
     * Returns the download directory name for a source.
     *
     * @param source the source to query.
     */
    fun getSourceDirName(source: Source): String {
        return source.toString()
    }

    /**
     * Returns the download directory name for a manga.
     *
     * @param manga the manga to query.
     */
    fun getMangaDirName(manga: Manga): String {
        return DiskUtil.buildValidFilename(manga.title)
    }

    /**
     * Returns the chapter directory name for a chapter.
     *
     * @param chapter the chapter to query.
     */
    fun getChapterDirName(chapter: Chapter): String {
        return DiskUtil.buildValidFilename(chapter.name)
    }
}
package template.data.download

import android.content.Context
import android.net.Uri
import com.hippo.unifile.UniFile
import template.App
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.extensions.getOrDefault
import template.source.SourceManager
import template.data.preference.PreferencesHelper
import java.util.concurrent.TimeUnit

/**
 * Cache where we dump the downloads directory from the filesystem. This class is needed because
 * directory checking is expensive and it slowdowns the app. The cache is invalidated by the time
 * defined in [renewInterval] as we don't have any control over the filesystem and the user can
 * delete the folders at any time without th app noticing.
 *
 * @param context the application context.
 * @param pathProvider the downloads directories path provider.
 * @param sourceManager the source manager.
 * @param preferences the preferences of the app.
 */
class DownloadCache(private val context: Context,
                    private val pathProvider: DownloadPathProvider,
                    private val sourceManager: SourceManager = App.app.lazySourceManager.get(),
                    private val preferences: PreferencesHelper = App.app.lazyPreferencesHelper.get()) {

    /**
     * The interval after which this cache should be invalidated. 1 hour shouldn't cause major
     * issues, as the cache is only used for UI feedback.
     */
    private val renewInterval = TimeUnit.HOURS.toMillis(1)

    /**
     * The last time the cache was refreshed.
     */
    private var lastRenew = 0L

    /**
     * The root directory for downloads.
     */
    private var rootDir = RootDirectory(getDirectoryFromPreference())

    init {
        preferences.downloadsDirectory().asObservable()
                .skip(1)
                .subscribe {
                    lastRenew = 0L  // invalidate cache
                    rootDir = RootDirectory(getDirectoryFromPreference())
                }
    }

    /**
     * Returns the downloads directory from the user's preferences.
     */
    private fun getDirectoryFromPreference(): UniFile {
        val dir = preferences.downloadsDirectory().getOrDefault()
        return UniFile.fromUri(context, Uri.parse(dir))
    }

    /**
     * Returns true if the chapter is downloaded.
     *
     * @param chapter the chapter to check.
     * @param manga the manga of the chapter.
     * @param skipCache whether to skip the directory cache and check in the filesystem.
     */
    fun isChapterDownloaded(chapter: Chapter, manga: Manga, skipCache: Boolean): Boolean {
        if (skipCache) {
            val source = sourceManager.get(manga.source) ?: return false
            return pathProvider.findChapterDir(chapter, manga, source) != null
        }

        checkRenew()

        val sourceDir = rootDir.files[manga.source]
        if (sourceDir != null) {
            val mangaDir = sourceDir.files[pathProvider.getMangaDirName(manga)]
            if (mangaDir != null) {
                return pathProvider.getChapterDirName(chapter) in mangaDir.files
            }
        }
        return false
    }

    /**
     * Returns the amount of downloaded chapters for a manga.
     *
     * @param manga the manga to check.
     */
    fun getDownloadCount(manga: Manga): Int {
        checkRenew()

        val sourceDir = rootDir.files[manga.source]
        if (sourceDir != null) {
            val mangaDir = sourceDir.files[pathProvider.getMangaDirName(manga)]
            if (mangaDir != null) {
                return mangaDir.files.size
            }
        }
        return 0
    }

    /**
     * Checks if the cache needs a renewal and performs it if needed.
     */
    @Synchronized
    private fun checkRenew() {
        if (lastRenew + renewInterval < System.currentTimeMillis()) {
            renew()
            lastRenew = System.currentTimeMillis()
        }
    }

    /**
     * Renews the downloads cache.
     */
    private fun renew() {
        val onlineSources = sourceManager.getOnlineSources()

        val sourceDirs = rootDir.dir.listFiles()
                .orEmpty()
                // 通过指定的条件，把list转换成map
                .associate {
                    it.name to SourceDirectory(it)
                }
                .mapNotNullKeys { entry ->
                    onlineSources.find {
                        pathProvider.getSourceDirName(it) == entry.key
                    }?.id
                }

        rootDir.files = sourceDirs

        sourceDirs.values.forEach { sourceDir ->
            val mangaDirs = sourceDir.dir.listFiles()
                    .orEmpty()
                    .associateNotNullKeys {
                        it.name to MangaDirectory(it)
                    }

            sourceDir.files = mangaDirs

            mangaDirs.values.forEach { mangaDir ->
                val chapterDirs = mangaDir.dir.listFiles()
                        .orEmpty()
                        .mapNotNull {
                            it.name
                        }
                        .toHashSet()

                mangaDir.files = chapterDirs
            }
        }
    }

    /**
     * Adds a chapter that has just been download to this cache.
     *
     * @param chapterDirName the downloaded chapter's directory name.
     * @param mangaUniFile the directory of the manga.
     * @param manga the manga of the chapter.
     */
    @Synchronized
    fun addChapter(chapterDirName: String, mangaUniFile: UniFile, manga: Manga) {
        // Retrieve the cached source directory or cache a new one
        var sourceDir = rootDir.files[manga.source]
        if (sourceDir == null) {
            val source = sourceManager.get(manga.source) ?: return
            val sourceUniFile = pathProvider.findSourceDir(source) ?: return
            sourceDir = SourceDirectory(sourceUniFile)
            rootDir.files += manga.source to sourceDir
        }

        // Retrieve the cached manga directory or cache a new one
        val mangaDirName = pathProvider.getMangaDirName(manga)
        var mangaDir = sourceDir.files[mangaDirName]
        if (mangaDir == null) {
            mangaDir = MangaDirectory(mangaUniFile)
            sourceDir.files += mangaDirName to mangaDir
        }

        // Save the chapter directory
        mangaDir.files += chapterDirName
    }

    /**
     * Removes a chapter that has been deleted from this cache.
     *
     * @param chapter the chapter to remove.
     * @param manga the manga of the chapter.
     */
    @Synchronized
    fun removeChapter(chapter: Chapter, manga: Manga) {
        val sourceDir = rootDir.files[manga.source] ?: return
        val mangaDir = sourceDir.files[pathProvider.getMangaDirName(manga)] ?: return
        val chapterDirName = pathProvider.getChapterDirName(chapter)
        if (chapterDirName in mangaDir.files) {
            mangaDir.files -= chapterDirName
        }
    }

    /**
     * Removes a manga that has been deleted from this cache.
     *
     * @param manga the manga to remove.
     */
    @Synchronized
    fun removeManga(manga: Manga) {
        val sourceDir = rootDir.files[manga.source] ?: return
        val mangaDirName = pathProvider.getMangaDirName(manga)
        if (mangaDirName in sourceDir.files) {
            sourceDir.files -= mangaDirName
        }
    }

    /**
     * Class to store the files under the root downloads directory.
     */
    private class RootDirectory(val dir: UniFile,
                                var files: Map<Long, SourceDirectory> = hashMapOf())

    /**
     * Class to store the files under a source directory.
     */
    private class SourceDirectory(val dir: UniFile,
                                  var files: Map<String, MangaDirectory> = hashMapOf())

    /**
     * Class to store the files under a manga directory.
     */
    private class MangaDirectory(val dir: UniFile,
                                 var files: Set<String> = hashSetOf())

    /**
     * Returns a new map containing only the key entries of [transform] that are not null.
     */
    private inline fun <K, V, R> Map<out K, V>.mapNotNullKeys(transform: (Map.Entry<K?, V>) -> R?): Map<R, V> {
        val destination = LinkedHashMap<R, V>()
        forEach { element ->
            transform(element)?.let {
                destination.put(it, element.value)
            }
        }
        return destination
    }

    /**
     * Returns a map from a list containing only the key entries of [transform] that are not null.
     */
    private inline fun <T, K, V> Array<T>.associateNotNullKeys(transform: (T) -> Pair<K?, V>): Map<K, V> {
        val destination = LinkedHashMap<K, V>()
        for (element in this) {
            val (key, value) = transform(element)
            if (key != null) {
                destination[key] = value
            }
        }
        return destination
    }
}

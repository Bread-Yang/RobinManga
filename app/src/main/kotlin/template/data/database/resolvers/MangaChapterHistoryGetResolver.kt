package template.data.database.resolvers

import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import template.data.database.mappers.ChapterGetResolver
import template.data.database.mappers.HistoryGetResolver
import template.data.database.mappers.MangaGetResolver
import template.data.database.models.MangaChapterHistory

class MangaChapterHistoryGetResolver : DefaultGetResolver<MangaChapterHistory>() {

    companion object {
        val INSTANCE = MangaChapterHistoryGetResolver()
    }

    /**
     * Manga get resolver
     */
    private val mangaGetResolver = MangaGetResolver()

    /**
     * Chapter gt resolver
     */
    private val chapterResolver = ChapterGetResolver()

    /**
     * History get resolver
     */
    private val historyGetResolver = HistoryGetResolver()

    /**
     * Map collect objects from cursor result
     */
    override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): MangaChapterHistory {
        // Get manga object
        val manga = mangaGetResolver.mapFromCursor(storIOSQLite, cursor)

        // Get chapter object
        val chapter = chapterResolver.mapFromCursor(storIOSQLite, cursor)

        // Get history object
        val history = historyGetResolver.mapFromCursor(storIOSQLite, cursor)

        // Make certain column conflicts are dealt with
        manga.id = chapter.manga_id
        manga.url = cursor.getString(cursor.getColumnIndex("mangaUrl"))
        chapter.id = history.chapter_id

        // Return result
        return MangaChapterHistory(manga, chapter, history)
    }
}
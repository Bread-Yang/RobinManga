package template.data.database.queries

import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.RawQuery
import template.data.database.DbProvider
import template.data.database.models.History
import template.data.database.models.MangaChapterHistory
import template.data.database.resolvers.HistoryLastReadPutResolver
import template.data.database.resolvers.MangaChapterHistoryGetResolver
import template.data.database.tables.HistoryTable
import java.util.*

interface HistoryQueries : DbProvider {

    /**
     * Insert history into database
     * @param history object containing history information
     */
    fun insertHistory(history: History) = db.put().`object`(history).prepare()

    /**
     * Returns history of recent manga containing last read chapter
     * @param date recent date range
     */
    fun getRecentManga(date: Date) = db.get()
            .listOfObjects(MangaChapterHistory::class.java)
            .withQuery(RawQuery.builder()
                    .query(getRecentMangasQuery())
                    .args(date.time)
                    .observesTables(HistoryTable.TABLE)
                    .build())
            .withGetResolver(MangaChapterHistoryGetResolver.INSTANCE)
            .prepare()

    fun getHistoryByMangaId(mangaId: Long) = db.get()
            .listOfObjects(History::class.java)
            .withQuery(RawQuery.builder()
                    .query(getHistoryByMangaId())
                    .args(mangaId)
                    .observesTables(HistoryTable.TABLE)
                    .build())
            .prepare()

    /**
     * Updates the history last read.
     * Inserts history object if not yet in database
     * @param history history object
     */
    fun updateHistoryLastRead(history: History) = db.put()
            .`object`(history)
            .withPutResolver(HistoryLastReadPutResolver())
            .prepare()

    /**
     * Updates the history last read.
     * Inserts history object if not yet in database
     * @param historyList history object list
     */
    fun updateHistoryLastRead(historyList: List<History>) = db.put()
            .objects(historyList)
            .withPutResolver(HistoryLastReadPutResolver())
            .prepare()

    fun deleteHistoryNoLastRead() = db.delete()
            .byQuery(DeleteQuery.builder()
                    .table(HistoryTable.TABLE)
                    .where("${HistoryTable.COL_LAST_READ} = ?")
                    .whereArgs(0)
                    .build())
            .prepare()
}
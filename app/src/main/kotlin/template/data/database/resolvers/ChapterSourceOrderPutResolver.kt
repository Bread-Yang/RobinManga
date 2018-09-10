package template.data.database.resolvers

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import template.data.database.models.Chapter
import template.data.database.tables.ChapterTable
import template.extensions.inTransactionReturn

class ChapterSourceOrderPutResolver : PutResolver<Chapter>() {

    override fun performPut(db: StorIOSQLite, chapter: Chapter) = db.inTransactionReturn {
        val updateQuery = mapToUpdateQuery(chapter)
        val contentValues = mapToContentValues(chapter)

        val numberOfRowsUpdated = db.lowLevel().update(updateQuery, contentValues)
        PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
    }

    fun mapToUpdateQuery(chapter: Chapter) = UpdateQuery.builder()
            .table(ChapterTable.TABLE)
            .where("${ChapterTable.COL_URL} = ? AND ${ChapterTable.COL_MANGA_ID} = ?")
            .whereArgs(chapter.url, chapter.manga_id)
            .build()

    fun mapToContentValues(chapter: Chapter) = ContentValues(1).apply {
        put(ChapterTable.COL_SOURCE_ORDER, chapter.source_order)
    }

}
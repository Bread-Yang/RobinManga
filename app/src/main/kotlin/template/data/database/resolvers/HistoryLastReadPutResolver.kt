package template.data.database.resolvers

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult
import com.pushtorefresh.storio3.sqlite.queries.Query
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import template.data.database.mappers.HistoryPutResolver
import template.data.database.models.History
import template.data.database.tables.HistoryTable
import template.extensions.inTransaction
import template.extensions.inTransactionReturn

class HistoryLastReadPutResolver : HistoryPutResolver() {

    /**
     * Updates last_read time of chapter
     */
    override fun performPut(db: StorIOSQLite, history: History): PutResult = db.inTransactionReturn {
        val updateQuery = mapToUpdateQuery(history)

        val cursor = db.lowLevel().query(Query.builder()
                .table(updateQuery.table())
                .where(updateQuery.where())
                .whereArgs(updateQuery.whereArgs())
                .build())

        val putResult: PutResult

        try {
            putResult = if (cursor.count == 0) {
                val insertQuery = mapToInsertQuery(history)
                val insertedId = db.lowLevel().insert(insertQuery, mapToContentValues(history))
                PutResult.newInsertResult(insertedId, insertQuery.table())
            } else {
                val numberOfRowsUpdated = db.lowLevel().update(updateQuery, mapToUpdateContentValues(history))
                PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
            }
        } finally {
            cursor.close()
        }

        putResult
    }

    /**
     * Creates update query
     * @param obj history object
     */
    override fun mapToUpdateQuery(obj: History): UpdateQuery = UpdateQuery.builder()
            .table(HistoryTable.TABLE)
            .where("${HistoryTable.COL_CHAPTER_ID} = ?")
            .whereArgs(obj.chapter_id)
            .build()

    /**
     * Create content query
     * @param history object
     */
    fun mapToUpdateContentValues(history: History) = ContentValues(1).apply {
        put(HistoryTable.COL_LAST_READ, history.last_read)
    }
}

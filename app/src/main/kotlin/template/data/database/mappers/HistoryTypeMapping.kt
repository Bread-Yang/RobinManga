package template.data.database.mappers

import android.content.ContentValues
import android.database.Cursor
import com.pushtorefresh.storio3.sqlite.SQLiteTypeMapping
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio3.sqlite.operations.get.DefaultGetResolver
import com.pushtorefresh.storio3.sqlite.operations.put.DefaultPutResolver
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.InsertQuery
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import template.data.database.models.History
import template.data.database.models.HistoryImpl
import template.data.database.tables.HistoryTable.COL_CHAPTER_ID
import template.data.database.tables.HistoryTable.COL_ID
import template.data.database.tables.HistoryTable.COL_LAST_READ
import template.data.database.tables.HistoryTable.COL_TIME_READ
import template.data.database.tables.HistoryTable.TABLE

class HistoryTypeMapping : SQLiteTypeMapping<History>(
        HistoryPutResolver(),
        HistoryGetResolver(),
        HistoryDeleteResolver()
)

open class HistoryPutResolver : DefaultPutResolver<History>() {

    override fun mapToInsertQuery(`object`: History) = InsertQuery.builder()
            .table(TABLE)
            .build()

    override fun mapToUpdateQuery(obj: History) = UpdateQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()

    override fun mapToContentValues(obj: History) = ContentValues(4).apply {
        put(COL_ID, obj.id)
        put(COL_CHAPTER_ID, obj.chapter_id)
        put(COL_LAST_READ, obj.last_read)
        put(COL_TIME_READ, obj.time_read)
    }
}

class HistoryGetResolver : DefaultGetResolver<History>() {

    override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): History = HistoryImpl().apply {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        chapter_id = cursor.getLong(cursor.getColumnIndex(COL_CHAPTER_ID))
        last_read = cursor.getLong(cursor.getColumnIndex(COL_LAST_READ))
        time_read = cursor.getLong(cursor.getColumnIndex(COL_TIME_READ))
    }
}

class HistoryDeleteResolver : DefaultDeleteResolver<History>() {

    override fun mapToDeleteQuery(obj: History) = DeleteQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()
}

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
import template.data.database.models.Chapter
import template.data.database.models.ChapterImpl
import template.data.database.tables.ChapterTable.COL_BOOKMARK
import template.data.database.tables.ChapterTable.COL_CHAPTER_NUMBER
import template.data.database.tables.ChapterTable.COL_DATE_FETCH
import template.data.database.tables.ChapterTable.COL_DATE_UPLOAD
import template.data.database.tables.ChapterTable.COL_ID
import template.data.database.tables.ChapterTable.COL_LAST_PAGE_READ
import template.data.database.tables.ChapterTable.COL_MANGA_ID
import template.data.database.tables.ChapterTable.COL_NAME
import template.data.database.tables.ChapterTable.COL_READ
import template.data.database.tables.ChapterTable.COL_SCANLATOR
import template.data.database.tables.ChapterTable.COL_SOURCE_ORDER
import template.data.database.tables.ChapterTable.COL_URL
import template.data.database.tables.ChapterTable.TABLE

class ChapterTypeMapping : SQLiteTypeMapping<Chapter>(
        ChapterPutResolver(),
        ChapterGetResolver(),
        ChapterDeleteResolver()
)

class ChapterPutResolver : DefaultPutResolver<Chapter>() {

    override fun mapToInsertQuery(obj: Chapter) = InsertQuery.builder()
            .table(TABLE)
            .build()

    override fun mapToUpdateQuery(obj: Chapter) = UpdateQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()

    override fun mapToContentValues(obj: Chapter) = ContentValues(11).apply {
        put(COL_ID, obj.id)
        put(COL_MANGA_ID, obj.manga_id)
        put(COL_URL, obj.url)
        put(COL_NAME, obj.name)
        put(COL_READ, obj.read)
        put(COL_SCANLATOR, obj.scanlator)
        put(COL_BOOKMARK, obj.bookmark)
        put(COL_DATE_FETCH, obj.date_fetch)
        put(COL_DATE_UPLOAD, obj.date_upload)
        put(COL_LAST_PAGE_READ, obj.last_page_read)
        put(COL_CHAPTER_NUMBER, obj.chapter_number)
        put(COL_SOURCE_ORDER, obj.source_order)
    }
}

class ChapterGetResolver : DefaultGetResolver<Chapter>() {

    override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): Chapter = ChapterImpl().apply {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        manga_id = cursor.getLong(cursor.getColumnIndex(COL_MANGA_ID))
        url = cursor.getString(cursor.getColumnIndex(COL_URL))
        name = cursor.getString(cursor.getColumnIndex(COL_NAME))
        scanlator = cursor.getString(cursor.getColumnIndex(COL_SCANLATOR))
        read = cursor.getInt(cursor.getColumnIndex(COL_READ)) == 1
        bookmark = cursor.getInt(cursor.getColumnIndex(COL_BOOKMARK)) == 1
        date_fetch = cursor.getLong(cursor.getColumnIndex(COL_DATE_FETCH))
        date_upload = cursor.getLong(cursor.getColumnIndex(COL_DATE_UPLOAD))
        last_page_read = cursor.getInt(cursor.getColumnIndex(COL_LAST_PAGE_READ))
        chapter_number = cursor.getFloat(cursor.getColumnIndex(COL_CHAPTER_NUMBER))
        source_order = cursor.getInt(cursor.getColumnIndex(COL_SOURCE_ORDER))
    }
}

class ChapterDeleteResolver : DefaultDeleteResolver<Chapter>() {

    override fun mapToDeleteQuery(obj: Chapter) = DeleteQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()
}


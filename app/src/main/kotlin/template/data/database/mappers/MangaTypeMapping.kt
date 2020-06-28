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
import template.data.database.models.Manga
import template.data.database.models.MangaImpl
import template.data.database.tables.MangaTable.COL_ARTIST
import template.data.database.tables.MangaTable.COL_AUTHOR
import template.data.database.tables.MangaTable.COL_CHAPTER_FLAGS
import template.data.database.tables.MangaTable.COL_DESCRIPTION
import template.data.database.tables.MangaTable.COL_FAVORITE
import template.data.database.tables.MangaTable.COL_GENRE
import template.data.database.tables.MangaTable.COL_ID
import template.data.database.tables.MangaTable.COL_INITIALIZED
import template.data.database.tables.MangaTable.COL_LAST_UPDATE
import template.data.database.tables.MangaTable.COL_SOURCE
import template.data.database.tables.MangaTable.COL_STATUS
import template.data.database.tables.MangaTable.COL_THUMBNAIL_URL
import template.data.database.tables.MangaTable.COL_TITLE
import template.data.database.tables.MangaTable.COL_URL
import template.data.database.tables.MangaTable.COL_VIEWER
import template.data.database.tables.MangaTable.TABLE

open class MangaTypeMapping : SQLiteTypeMapping<Manga>(
        MangaPutResolver(),
        MangaGetResolver(),
        MangaDeleteResolver()
)

class MangaPutResolver : DefaultPutResolver<Manga>() {

    override fun mapToInsertQuery(`object`: Manga) = InsertQuery.builder()
            .table(TABLE)
            .build()

    override fun mapToUpdateQuery(`object`: Manga) = UpdateQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(`object`.id)
            .build()

    override fun mapToContentValues(obj: Manga) = ContentValues(15).apply {
        put(COL_ID, obj.id)
        put(COL_SOURCE, obj.source)
        put(COL_URL, obj.url)
        put(COL_ARTIST, obj.artist)
        put(COL_AUTHOR, obj.author)
        put(COL_DESCRIPTION, obj.description)
        put(COL_GENRE, obj.genre)
        put(COL_TITLE, obj.title)
        put(COL_STATUS, obj.status)
        put(COL_THUMBNAIL_URL, obj.thumbnail_url)
        put(COL_FAVORITE, obj.favorite)
        put(COL_LAST_UPDATE, obj.last_update)
        put(COL_INITIALIZED, obj.initialized)
        put(COL_VIEWER, obj.viewer)
        put(COL_CHAPTER_FLAGS, obj.chapter_flags)
    }
}

interface BaseMangaGetResolver {
    fun mapBaseFromCursor(manga: Manga, cursor: Cursor) = manga.apply {
        id = cursor.getLong(cursor.getColumnIndex(COL_ID))
        source = cursor.getLong(cursor.getColumnIndex(COL_SOURCE))
        url = cursor.getString(cursor.getColumnIndex(COL_URL))
        artist = cursor.getString(cursor.getColumnIndex(COL_ARTIST))
        author = cursor.getString(cursor.getColumnIndex(COL_AUTHOR))
        description = cursor.getString(cursor.getColumnIndex(COL_DESCRIPTION))
        genre = cursor.getString(cursor.getColumnIndex(COL_GENRE))
        title = cursor.getString(cursor.getColumnIndex(COL_TITLE))
        status = cursor.getInt(cursor.getColumnIndex(COL_STATUS))
        thumbnail_url = cursor.getString(cursor.getColumnIndex(COL_THUMBNAIL_URL))
        favorite = cursor.getInt(cursor.getColumnIndex(COL_FAVORITE)) == 1
        last_update = cursor.getLong(cursor.getColumnIndex(COL_LAST_UPDATE))
        initialized = cursor.getInt(cursor.getColumnIndex(COL_INITIALIZED)) == 1
        viewer = cursor.getInt(cursor.getColumnIndex(COL_VIEWER))
        chapter_flags = cursor.getInt(cursor.getColumnIndex(COL_CHAPTER_FLAGS))
    }
}

open class MangaGetResolver : DefaultGetResolver<Manga>(), BaseMangaGetResolver {

    override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): Manga {
        return mapBaseFromCursor(MangaImpl(), cursor)
    }
}

class MangaDeleteResolver : DefaultDeleteResolver<Manga>() {

    override fun mapToDeleteQuery(obj: Manga) = DeleteQuery.builder()
            .table(TABLE)
            .where("$COL_ID = ?")
            .whereArgs(obj.id)
            .build()
}

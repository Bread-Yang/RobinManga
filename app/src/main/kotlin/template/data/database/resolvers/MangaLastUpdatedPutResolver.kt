package template.data.database.resolvers

import android.content.ContentValues
import com.pushtorefresh.storio3.sqlite.StorIOSQLite
import com.pushtorefresh.storio3.sqlite.operations.put.PutResolver
import com.pushtorefresh.storio3.sqlite.operations.put.PutResult
import com.pushtorefresh.storio3.sqlite.queries.UpdateQuery
import template.data.database.models.Manga
import template.data.database.tables.MangaTable
import template.extensions.inTransactionReturn

class MangaLastUpdatedPutResolver : PutResolver<Manga>() {

    override fun performPut(db: StorIOSQLite, manga: Manga) = db.inTransactionReturn {
        val updateQuery = mapToUpdateQuery(manga)
        val contentValues = mapToContentValues(manga)

        val numberOfRowsUpdated = db.lowLevel().update(updateQuery, contentValues)
        PutResult.newUpdateResult(numberOfRowsUpdated, updateQuery.table())
    }

    fun mapToUpdateQuery(manga: Manga) = UpdateQuery.builder()
            .table(MangaTable.TABLE)
            .where("${MangaTable.COL_ID} = ?")
            .whereArgs(manga.id)
            .build()

    fun mapToContentValues(manga: Manga) = ContentValues(1).apply {
        put(MangaTable.COL_LAST_UPDATE, manga.last_update)
    }

}


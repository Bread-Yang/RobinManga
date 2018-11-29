package template.data.database

import android.content.Context
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite
import template.data.database.mappers.ChapterTypeMapping
import template.data.database.mappers.MangaTypeMapping
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.data.database.queries.CategoryQueries
import template.data.database.queries.ChapterQueries
import template.data.database.queries.MangaCategoryQueries
import template.data.database.queries.MangaQueries
import template.extensions.inTransaction

/**
 * This class provides operations to manage the database through its interfaces.
 */
open class DatabaseHelper(context: Context)
    : MangaQueries, ChapterQueries, CategoryQueries, MangaCategoryQueries {

    // TODO("剩下的表创建")
    override val db: DefaultStorIOSQLite =
            DefaultStorIOSQLite.builder()
                    .sqliteOpenHelper(DbOpenHelper(context))
                    .addTypeMapping(Manga::class.java, MangaTypeMapping())
                    .addTypeMapping(Chapter::class.java, ChapterTypeMapping())
                    .build()

    inline fun inTransaction(block: () -> Unit) = db.inTransaction(block)

    fun lowLevel() = db.lowLevel()
}
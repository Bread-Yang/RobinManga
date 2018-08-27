package template.data.database

import android.content.Context
import com.pushtorefresh.storio3.sqlite.impl.DefaultStorIOSQLite
import template.data.database.mappers.MangaTypeMapping
import template.data.database.models.Manga
import template.data.database.queries.MangaQueries
import template.extensions.inTransaction

/**
 * This class provides operations to manage the database through its interfaces.
 */
open class DatabaseHelper(context: Context) : MangaQueries {

    // TODO("剩下的表创建")
    override val db: DefaultStorIOSQLite =
            DefaultStorIOSQLite.builder()
                    .sqliteOpenHelper(DbOpenHelper(context))
                    .addTypeMapping(Manga::class.java, MangaTypeMapping())
                    .build()

    inline fun inTransaction(block: () -> Unit) = db.inTransaction(block)

    fun lowLevel() = db.lowLevel()
}
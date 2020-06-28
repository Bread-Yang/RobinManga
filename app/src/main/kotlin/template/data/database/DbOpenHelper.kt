package template.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import template.data.database.tables.CategoryTable
import template.data.database.tables.ChapterTable
import template.data.database.tables.MangaTable

class DbOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        /**
         * Name of the database file.
         */
        const val DATABASE_NAME = "robin_manga.databaseHelper"

        /**
         * Version of the database.
         */
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) = with(db) {
        execSQL(MangaTable.createTableQuery)
        execSQL(ChapterTable.createTableQuery)
        execSQL(CategoryTable.createTableQuery)
        // TODO("剩下的数据库")

        // DB indexes.
        execSQL(MangaTable.createUrlIndexQuery)
        execSQL(MangaTable.createFavoriteIndexQuery)
        execSQL(ChapterTable.createMangaIdIndexQuery)
        // TODO("剩下的约束")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    // 外键约束用来强制 两个表之间”存在”的关系
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }
}
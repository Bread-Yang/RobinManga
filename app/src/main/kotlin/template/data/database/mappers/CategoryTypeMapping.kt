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
import template.data.database.models.Category
import template.data.database.models.CategoryImpl
import template.data.database.tables.CategoryTable

class CategoryTypeMapping : SQLiteTypeMapping<Category>(
        CategoryPutResolver(),
        CategoryGetResolver(),
        CategoryDeleteResolver()
)

class CategoryPutResolver : DefaultPutResolver<Category>() {

    override fun mapToInsertQuery(obj: Category) = InsertQuery.builder()
            .table(CategoryTable.TABLE)
            .build()

    override fun mapToUpdateQuery(obj: Category) = UpdateQuery.builder()
            .table(CategoryTable.TABLE)
            .where("${CategoryTable.COL_ID} = ?")
            .whereArgs(obj.id)
            .build()

    override fun mapToContentValues(obj: Category) = ContentValues(4).apply {
        put(CategoryTable.COL_ID, obj.id)
        put(CategoryTable.COL_NAME, obj.name)
        put(CategoryTable.COL_ORDER, obj.order)
        put(CategoryTable.COL_FLAGS, obj.flags)
    }
}

class CategoryGetResolver : DefaultGetResolver<Category>() {

    override fun mapFromCursor(storIOSQLite: StorIOSQLite, cursor: Cursor): Category {
        return CategoryImpl().apply {
            id = cursor.getInt(cursor.getColumnIndex(CategoryTable.COL_ID))
            name = cursor.getString(cursor.getColumnIndex(CategoryTable.COL_NAME))
            order = cursor.getInt(cursor.getColumnIndex(CategoryTable.COL_ORDER))
            flags = cursor.getInt(cursor.getColumnIndex(CategoryTable.COL_FLAGS))
        }
    }
}

class CategoryDeleteResolver : DefaultDeleteResolver<Category>() {

    override fun mapToDeleteQuery(obj: Category): DeleteQuery {
        return DeleteQuery.builder()
                .table(CategoryTable.TABLE)
                .where("${CategoryTable.COL_ID} = ?")
                .whereArgs(obj.id)
                .build()
    }
}
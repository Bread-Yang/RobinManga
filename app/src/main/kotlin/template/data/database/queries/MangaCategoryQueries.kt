package template.data.database.queries

import com.pushtorefresh.storio3.Queries
import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import template.data.database.DbProvider
import template.data.database.models.Manga
import template.data.database.models.MangaCategory
import template.data.database.tables.MangaCategoryTable
import template.extensions.inTransaction

interface MangaCategoryQueries : DbProvider {

    fun insertMangaCategory(mangaCategory: MangaCategory) = db.put().`object`(mangaCategory).prepare()

    fun insertMangaCategories(mangasCategories: List<MangaCategory>) = db.put().objects(mangasCategories).prepare()

    fun deleteOldMangasCategories(mangas: List<Manga>) = db.delete()
            .byQuery(DeleteQuery.builder()
                    .table(MangaCategoryTable.TABLE)
                    .where("${MangaCategoryTable.COL_MANGA_ID} IN （${Queries.placeholders(mangas.size)}）")
                    .whereArgs(*mangas.map { it.id }.toTypedArray())    // * : 为了将数组展开并传入可变参数，Kotlin使用星号（*）操作符将数组进行展开
                    .build())
            .prepare()

    fun setMangaCategories(mangasCategories: List<MangaCategory>, mangas: List<Manga>) {
        db.inTransaction {
            deleteOldMangasCategories(mangas).executeAsBlocking()
            insertMangaCategories(mangasCategories).executeAsBlocking()
        }
    }
}

package template.data.database.queries

import com.pushtorefresh.storio3.sqlite.queries.DeleteQuery
import com.pushtorefresh.storio3.sqlite.queries.Query
import template.data.database.DbProvider
import template.data.database.models.Manga
import template.data.database.resolvers.MangaLastUpdatedPutResolver
import template.data.database.tables.MangaTable

interface MangaQueries : DbProvider {

    fun getMangas() = db.get()
            .listOfObjects(Manga::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .build())
            .prepare()

//    fun getLibraryMangas() = databaseHelper.get()
//            .listOfObjects(LibraryManga::class.java)
//            .withQuery(RawQuery.builder()
//                    .query(libraryQuery))

    fun getManga(url: String, sourceId: Long) = db.get()
            .`object`(Manga::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .where("${MangaTable.COL_URL} = ? AND ${MangaTable.COL_SOURCE} = ?")
                    .whereArgs(url, sourceId)
                    .build())
            .prepare()

    fun getManga(id: Long) = db.get()
            .`object`(Manga::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .where("${MangaTable.COL_ID} = ?")
                    .whereArgs(id)
                    .build())
            .prepare()

    fun insertManga(manga: Manga) = db.put().`object`(manga).prepare()

    fun insertMangas(mangas: List<Manga>) = db.put().objects(mangas).prepare()

    fun updateLastUpdated(manga: Manga) = db.put()
            .`object`(manga)
            .withPutResolver(MangaLastUpdatedPutResolver())
            .prepare()

    fun deleteMangasNotInLibrary() = db.delete()
            .byQuery(DeleteQuery.builder()
                    .table(MangaTable.TABLE)
                    .where("${MangaTable.COL_FAVORITE} = ?")
                    .whereArgs(0)
                    .build())
            .prepare()

}
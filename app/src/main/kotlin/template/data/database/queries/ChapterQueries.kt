package template.data.database.queries

import com.pushtorefresh.storio3.sqlite.queries.Query
import template.data.database.DbProvider
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.data.database.resolvers.ChapterSourceOrderPutResolver
import template.data.database.tables.ChapterTable

interface ChapterQueries : DbProvider {

    fun getChapters(manga: Manga) = db.get()
            .listOfObjects(Chapter::class.java)
            .withQuery(Query.builder()
                    .table(ChapterTable.TABLE)
                    .where("${ChapterTable.COL_MANGA_ID} = ?")
                    .whereArgs(manga.id)
                    .build())
            .prepare()

    fun getChapter(id: Long) = db.get()
            .`object`(Chapter::class.java)
            .withQuery(Query.builder()
                    .table(ChapterTable.TABLE)
                    .where("${ChapterTable.COL_ID} = ?")
                    .whereArgs(id)
                    .build())
            .prepare()

    fun insertChapter(chapter: Chapter) = db.put().`object`(chapter).prepare()

    fun insertChapters(chapters: List<Chapter>) = db.put().objects(chapters).prepare()

    fun deleteChapters(chapters: List<Chapter>) = db.delete().objects(chapters).prepare()

    fun fixChaptersSourceOrder(chapters: List<Chapter>) = db.put()
            .objects(chapters)
            .withPutResolver(ChapterSourceOrderPutResolver())
            .prepare()
}
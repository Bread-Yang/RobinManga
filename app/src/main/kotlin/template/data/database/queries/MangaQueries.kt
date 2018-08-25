package template.data.database.queries

import com.pushtorefresh.storio3.sqlite.queries.Query
import template.data.database.DbProvider
import template.data.database.models.Manga

/**
 * Created by Robin Yeung on 8/25/18.
 */
interface MangaQueries : DbProvider {

    fun getMangas() = db.get()
            .listOfObjects(Manga::class.java)
            .withQuery(Query.builder()
                    .table(MangaTable.TABLE)
                    .build())
            .prepare()
}
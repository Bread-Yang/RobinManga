package template.data.database.queries

import template.data.database.tables.CategoryTable
import template.data.database.tables.MangaCategoryTable

/**
 * SQL的各种连接Join详解 : https://www.cnblogs.com/reaptomorrow-flydream/p/8145610.html
 */
/**
 * Query to get the categories for a manga.
 */
fun getCategoriesForMangaQuery() = """
    SELECT ${CategoryTable.TABLE}.* FROM ${CategoryTable.TABLE}
    JOIN ${MangaCategoryTable.TABLE} ON ${CategoryTable.TABLE}.${CategoryTable.COL_ID} =
    ${MangaCategoryTable.TABLE}.${MangaCategoryTable.COL_CATEGORY_ID}
    WHERE ${MangaCategoryTable.COL_MANGA_ID} = ?
"""
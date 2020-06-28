package template.data.database.queries

import template.data.database.tables.*

/**
 * SQL的各种连接Join详解 : https://www.cnblogs.com/reaptomorrow-flydream/p/8145610.html
 */

/**
 * Query to get the recently read chapters of manga from the library up to a date.
 * The max_last_read table contains the most recent chapters grouped by manga
 * The select statement returns all information of chapters that have the same id as the chapter in max_last_read
 * @return return limit is 25
 */
fun getRecentMangasQuery() = """
    SELECT ${MangaTable.TABLE}.${MangaTable.COL_URL} as mangaUrl, ${MangaTable.TABLE}.*, ${ChapterTable.TABLE}.*, ${HistoryTable.TABLE}.*
    FROM ${MangaTable.TABLE}
    JOIN ${ChapterTable.TABLE}
    ON ${MangaTable.TABLE}.${MangaTable.COL_ID} = ${ChapterTable.TABLE}.${ChapterTable.COL_MANGA_ID}
    JOIN ${HistoryTable.TABLE}
    ON ${ChapterTable.TABLE}.${ChapterTable.COL_ID} = ${HistoryTable.TABLE}.${HistoryTable.COL_CHAPTER_ID}
    JOIN (
    SELECT ${ChapterTable.TABLE}.${ChapterTable.COL_MANGA_ID},${ChapterTable.TABLE}.${ChapterTable.COL_ID} as ${HistoryTable.COL_CHAPTER_ID}, MAX(${HistoryTable.TABLE}.${HistoryTable.COL_LAST_READ}) as ${HistoryTable.COL_LAST_READ}
    FROM ${ChapterTable.TABLE} JOIN ${HistoryTable.TABLE}
    ON ${ChapterTable.TABLE}.${ChapterTable.COL_ID} = ${HistoryTable.TABLE}.${HistoryTable.COL_CHAPTER_ID}
    GROUP BY ${ChapterTable.TABLE}.${ChapterTable.COL_MANGA_ID}) AS max_last_read
    ON ${ChapterTable.TABLE}.${ChapterTable.COL_MANGA_ID} = max_last_read.${ChapterTable.COL_MANGA_ID}
    WHERE ${HistoryTable.TABLE}.${HistoryTable.COL_LAST_READ} > ? AND max_last_read.${HistoryTable.COL_CHAPTER_ID} = ${HistoryTable.TABLE}.${HistoryTable.COL_CHAPTER_ID}
    ORDER BY max_last_read.${HistoryTable.COL_LAST_READ} DESC
    LIMIT 25
"""

fun getHistoryByMangaId() = """
    SELECT ${HistoryTable.TABLE}.*
    FROM ${HistoryTable.TABLE}
    JOIN ${ChapterTable.TABLE}
    ON ${HistoryTable.TABLE}.${HistoryTable.COL_CHAPTER_ID} = ${ChapterTable.TABLE}.${ChapterTable.COL_ID}
    WHERE ${ChapterTable.TABLE}.${ChapterTable.COL_MANGA_ID} =
    ? AND ${HistoryTable.TABLE}.${HistoryTable.COL_CHAPTER_ID} = ${ChapterTable.TABLE}.${ChapterTable.COL_ID}
"""

/**
 * Query to get the categories for a manga.
 */
fun getCategoriesForMangaQuery() = """
    SELECT ${CategoryTable.TABLE}.* FROM ${CategoryTable.TABLE}
    JOIN ${MangaCategoryTable.TABLE} ON ${CategoryTable.TABLE}.${CategoryTable.COL_ID} =
    ${MangaCategoryTable.TABLE}.${MangaCategoryTable.COL_CATEGORY_ID}
    WHERE ${MangaCategoryTable.COL_MANGA_ID} = ?
"""
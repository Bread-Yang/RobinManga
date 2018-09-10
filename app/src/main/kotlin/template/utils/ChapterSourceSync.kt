package template.utils

import template.data.database.DatabaseHelper
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.source.Source
import template.source.model.SChapter
import template.source.online.HttpSource
import java.util.*

/**
 * Helper method for syncing the list of chapter from the source with the ones from the database.
 *
 * @param db the database.
 * @param rawSourceChapters a list of chapter from the source.
 * @param manga the manga of the chapter
 * @param source the source of the chapter.
 * @return a pair of new insertions and deletions.
 */
fun syncChaptersWithSource(db: DatabaseHelper, rawSourceChapters: List<SChapter>, manga: Manga,
                           source: Source): Pair<List<Chapter>, List<Chapter>> {
    if (rawSourceChapters.isEmpty()) {
        throw Exception("No chapter found")
    }

    // Chapters from db.
    val dbChapters = db.getChapters(manga).executeAsBlocking()!!

    val sourceChapters = rawSourceChapters.mapIndexed { index, sChapter ->
        Chapter.create().apply {
            copyFrom(sChapter)
            manga_id = manga.id
            source_order = index
        }
    }

    // Chapters from the source not in db.
    val toAdd = mutableListOf<Chapter>()

    // Chapters whose metadata have changed.
    val toChange = mutableListOf<Chapter>()

    for (sourceChapter in sourceChapters) {
        val dbChapter = dbChapters.find {
            it.url == sourceChapter.url
        }

        // Add the chapter if not in db already, or update if the metadata changed.
        if (dbChapter == null) {
            toAdd.add(sourceChapter)
        } else {
            // this forces metadata update for the main viewable things in the chapter list
            ChapterRecognition.parseChapterNumber(sourceChapter, manga)
            if (shouldUpdateDbChapter(dbChapter, sourceChapter)) {
                dbChapter.scanlator = sourceChapter.scanlator
                dbChapter.name = sourceChapter.name
                dbChapter.date_upload = sourceChapter.date_upload
                dbChapter.chapter_number = sourceChapter.chapter_number
                toChange.add(dbChapter)
            }
        }
    }

    // Recongnize number for new chapters.
    toAdd.forEach {
        if (source is HttpSource) {
            source.prepareNewChapter(it, manga)
        }
        ChapterRecognition.parseChapterNumber(it, manga)
    }

    // Chapters from the db not in the source.
    val toDelete = dbChapters.filterNot { dbChapter ->
        sourceChapters.any { sourceChapter ->
            dbChapter.url == sourceChapter.url
        }
    }

    // Return if there's nothing to add, delete or change, avoiding unnecessary db transactions.
    if (toAdd.isEmpty() && toDelete.isEmpty() && toChange.isEmpty()) {
        return Pair(emptyList(), emptyList())
    }

    val readded = mutableListOf<Chapter>()

    db.inTransaction {
        val deletedChapterNumbers = TreeSet<Float>()
        val deletedReadChapterNumbers = TreeSet<Float>()
        if (!toDelete.isEmpty()) {
            for (c in toDelete) {
                if (c.read) {
                    deletedReadChapterNumbers.add(c.chapter_number)
                }
                deletedChapterNumbers.add(c.chapter_number)
            }
            db.deleteChapters(toDelete).executeAsBlocking()
        }

        if (!toAdd.isEmpty()) {
            // Set the date fetch for new items in reverse order to allow another sorting method.
            // Sources MUST return the chapters from most to less recent, which is common.
            var now = Date().time

            for (i in toAdd.indices.reversed()) {
                val c = toAdd[i]
                c.date_fetch = now++
                // Try to mark already read chapters as read when the source deletes them
                if (c.isRecognizedNumber && c.chapter_number in deletedReadChapterNumbers) {
                    c.read = true
                }
                if (c.isRecognizedNumber && c.chapter_number in deletedChapterNumbers) {
                    readded.add(c)
                }
            }
            db.insertChapters(toAdd).executeAsBlocking()
        }

        if (!toChange.isEmpty()) {
            db.insertChapters(toChange).executeAsBlocking()
        }

        // Fix order in source.
        db.fixChaptersSourceOrder(sourceChapters).executeAsBlocking()

        // Set this manga as updated since chapters were changed
        manga.last_update = Date().time
        db.updateLastUpdated(manga).executeAsBlocking()
    }
    return Pair(toAdd.subtract(readded).toList(), toDelete.subtract(readded).toList())
}

// checks if the chapter in db needs updated
private fun shouldUpdateDbChapter(dbChapter: Chapter, sourceChapter: SChapter): Boolean {
    return dbChapter.scanlator != sourceChapter.scanlator || dbChapter.name != sourceChapter.name ||
            dbChapter.date_upload != sourceChapter.date_upload ||
            dbChapter.chapter_number != sourceChapter.chapter_number
}
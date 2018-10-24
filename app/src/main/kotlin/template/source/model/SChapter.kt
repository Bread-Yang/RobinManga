package template.source.model

import java.io.Serializable

/**
 * Created by Robin Yeung on 8/22/18.
 */
interface SChapter : Serializable {

    var url: String

    var name: String

    var date_upload: Long

    var chapter_number: Float

    // A scanlator is a person or group of people who work collaboratively to scan and then translate
    // manga so that fans all around the world can enjoy them.
    var scanlator: String?

    fun copyFrom(other: SChapter) {
        name = other.name
        url = other.url
        date_upload = other.date_upload
        chapter_number = other.chapter_number
        scanlator = other.scanlator
    }

    companion object {
        fun create(): SChapter {
            return SChapterImpl()
        }
    }

}
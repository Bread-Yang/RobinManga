package template.source.model

/**
 * Created by Robin Yeung on 8/22/18.
 */
class SChapterImpl : SChapter {

    override lateinit var url: String

    override lateinit var name: String

    override var date_upload: Long = 0

    override var chapter_number: Float = -1f

    override  var scanlator: String? = null

}
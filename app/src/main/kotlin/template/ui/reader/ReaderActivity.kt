package template.ui.reader

import android.content.Context
import android.content.Intent
import android.os.Bundle
import nucleus5.factory.RequiresPresenter
import template.R
import template.data.database.models.Chapter
import template.data.database.models.Manga
import template.ui.common.annotation.Layout
import template.ui.common.mvp.activity.NucleusDaggerActivity

/**
 * Created by Robin Yeung on 9/10/18.
 */
@Layout(R.layout.reader_activity)
@RequiresPresenter(ReaderPresenter::class)
class ReaderActivity : NucleusDaggerActivity<ReaderPresenter>() {

    companion object {
        @Suppress("unused")
        const val LEFT_TO_RIGHT = 1
        const val RIGHT_TO_LEFT = 2
        const val VERTICAL = 3
        const val WEBTOON = 4

        const val WHITE_THEME = 0
        const val BLACK_THEME = 1

        const val MENU_VISIBLE = "menu_visible"

        fun newIntent(context: Context, manga: Manga, chapter: Chapter): Intent {
//            SharedData.put(ReaderEvent(manga, chapter))
            return Intent(context, ReaderActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initPresenterOnce() {

    }
}
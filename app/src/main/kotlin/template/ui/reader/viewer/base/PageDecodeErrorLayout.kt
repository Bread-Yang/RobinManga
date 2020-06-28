package template.ui.reader.viewer.base

import android.net.Uri
import androidx.core.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.reader_page_decode_error.view.*
import template.R
import template.source.model.Page
import template.ui.reader.ReaderActivity

class PageDecodeErrorLayout(
        val view: View,
        val page: Page,
        val theme: Int,
        val retryListener: () -> Unit
) {

    init {
        val textColor = if (theme == ReaderActivity.BLACK_THEME)
            ContextCompat.getColor(view.context, R.color.textColorSecondaryDark)
        else
            ContextCompat.getColor(view.context, R.color.textColorSecondaryLight)

        view.decode_error_text.setTextColor(textColor)

        view.decode_retry.setOnClickListener {
            retryListener()
        }

        view.decode_open_browser.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(page.imageUrl))
            view.context.startActivity(intent)
        }

        if (page.imageUrl == null) {
            view.decode_open_browser.visibility = View.GONE
        }
    }

}

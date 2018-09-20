package template.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.common_view_empty.view.*
import template.R
import template.extensions.getResourceColor
import template.extensions.setVectorCompat

class EmptyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        RelativeLayout(context, attrs) {

    init {
        inflate(context, R.layout.common_view_empty, this)
    }

    /**
     * Hide the information view
     */
    fun hide() {
        this.visibility = View.GONE
    }

    /**
     * Show the information view
     * @param drawable icon of information view
     * @param textResource text of information view
     */
    fun show(drawable: Int, textResource: Int) {
        imageView.setVectorCompat(drawable, context.getResourceColor(android.R.attr.textColorHint))
        text_label.text = context.getString(textResource)
        this.visibility = View.VISIBLE
    }
}

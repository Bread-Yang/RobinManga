package template.widget

import android.content.Context
import androidx.annotation.StringRes
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.common_dialog_with_checkbox.view.*
import template.R
import template.extensions.inflate

class DialogCheckboxView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        LinearLayout(context, attrs) {

    init {
        addView(inflate(R.layout.common_dialog_with_checkbox))
    }

    fun setDescription(@StringRes id: Int) {
        tvDescription.text = context.getString(id)
    }

    fun setOptionDescription(@StringRes id: Int) {
        cbOption.text = context.getString(id)
    }

    fun isChecked(): Boolean {
        return cbOption.isChecked
    }
}
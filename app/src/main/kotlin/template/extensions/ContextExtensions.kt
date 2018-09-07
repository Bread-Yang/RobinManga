package template.extensions

import android.content.Context
import android.support.annotation.StringRes
import android.widget.Toast

/**
 * Display a toast in this context.
 *
 * @param resource the text resource.
 * @param duration the duratioin of the toast. Defaults to short.
 */
fun Context.toast(@StringRes resource: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resource, duration).show()
}

/**
 * Display a toast in this context.
 *
 * @param text the text to display.
 * @param duration the duration of the toast. Defaults to short.
 */
fun Context.toast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text.orEmpty(), duration).show()
}

/**
 * Returns the color for the given attribute.
 *
 * @param resource the attribute.
 */
fun Context.getResourceColor(@StringRes resource: Int): Int {
    val typedArray = obtainStyledAttributes(intArrayOf(resource))
    val attrValue = typedArray.getColor(0, 0)
    typedArray.recycle()
    return attrValue
}

package template.extensions

import android.content.Context
import android.support.annotation.StringRes

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

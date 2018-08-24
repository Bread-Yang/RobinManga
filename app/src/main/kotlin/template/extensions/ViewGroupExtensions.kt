package template.extensions

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Robin Yeung on 8/22/18.
 */
fun ViewGroup.inflate(@LayoutRes layout: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layout, this, attachToRoot)
}
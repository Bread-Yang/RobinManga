package template.extensions

import com.f2prateek.rx.preferences2.Preference

/**
 * Created by Robin Yeung on 8/22/18.
 */
fun <T> Preference<T>.getOrDefault(): T = get() ?: defaultValue()!!

fun Preference<Boolean>.invert(): Boolean = getOrDefault().let {
    set(!it)
    !it
}
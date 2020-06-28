package template.extensions

import com.f2prateek.rx.preferences2.Preference

fun <T> Preference<T>.getOrDefault(): T = get() ?: defaultValue()!!

fun Preference<Boolean>.invert(): Boolean = getOrDefault().let {
    set(!it)
    !it
}
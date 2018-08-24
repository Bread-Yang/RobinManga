package template.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * https://www.jianshu.com/p/8ed899775143
 *
 * Created by Robin Yeung on 8/23/18.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    val store = PersistentCookieStore(context)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        store.addAll(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return store.get(url)
    }
}
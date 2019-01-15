package template.source.online

import io.reactivex.Observable
import okhttp3.Response
import template.source.Source

/**
 * Created by Robin Yeung on 1/9/19.
 */
interface LoginSource : Source {

    fun isLogged(): Boolean

    fun login(username: String, password: String): Observable<Boolean>

    fun isAuthenticationSuccessful(response: Response): Boolean
}
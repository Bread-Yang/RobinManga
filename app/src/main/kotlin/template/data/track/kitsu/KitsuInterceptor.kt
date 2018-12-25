package template.data.track.kitsu

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by Robin Yeung on 19/12/2018.
 */
class KitsuInterceptor(val kitsu: Kitsu, val gson: Gson) : Interceptor {

    /**
     * OAuth object used for authenticated requests.
     */
    private var oauth: OAuth? = kitsu.restoreToken()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val currAuth = oauth ?: throw Exception("Not authenticated with Kitsu")

        val refreshToken = currAuth.refresh_token!!

        // Refresh access token if expired.
        if (currAuth.isExpired()) {
            val response = chain.proceed(KitsuApi.refreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                newAuth(gson.fromJson(response.body()!!.string(), OAuth::class.java))
            } else {
                response.close()
            }
        }

        // Add the authorization header to the original request.
        val authRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer ${oauth!!.access_token}")
                .header("Accept", "application/vnd.api+json")
                .header("Content-Type", "application/vnd.api+json")
                .build()

        return chain.proceed(authRequest)
    }

    fun newAuth(oath: OAuth?) {
        this.oauth = oauth
        kitsu.saveToken(oauth)
    }
}
package template.data.track.kitsu

/**
 * Created by Robin Yeung on 19/12/2018.
 */
data class OAuth(
        val access_token: String,
        val token_type: String,
        val created_at: Long,
        val expires_in: Long,
        val refresh_token: String?) {

    fun isExpired() = (System.currentTimeMillis() / 1000) > (created_at + expires_in - 3600)
}
package template.data.track.anilist

/**
 * Created by Robin Yeung on 12/20/18.
 */
data class OAuth(
        val access_token: String,
        val token_type: String,
        val expires: Long,
        val expires_in: Long
) {
    fun isExpired() = System.currentTimeMillis() > expires
}
package template.data.track.kitsu

import android.content.Context
import android.graphics.Color
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.Observable
import template.App
import template.R
import template.data.database.models.Track
import template.data.track.TrackService
import template.data.track.model.TrackSearch
import java.text.DecimalFormat

class Kitsu(private val context: Context, id: Int) : TrackService(id) {

    companion object {
        const val READING = 1
        const val COMPLETED = 2
        const val ON_HOLD = 3
        const val DROPPED = 4
        const val PLAN_TO_READ = 5

        const val DEFAULT_STATUS = READING
        const val DEFAULT_SCORE = 0f
    }

    override val name: String = "Kitsu"

    private val gson: Gson = App.app.lazyGson.get()

    private val interceptor by lazy {
        KitsuInterceptor(this, gson)
    }

    private val api by lazy {
        KitsuApi(client, interceptor)
    }

    override fun getLogo(): Int {
        return R.drawable.kitsu
    }

    override fun getLogoColor(): Int {
        return Color.rgb(51, 37, 50)
    }

    override fun getStatusList(): List<Int> {
        return listOf(READING, COMPLETED, ON_HOLD, DROPPED, PLAN_TO_READ)
    }

    override fun getStatus(status: Int): String = with(context) {
        when (status) {
            READING -> getString(R.string.reading)
            COMPLETED -> getString(R.string.completed)
            ON_HOLD -> getString(R.string.on_hold)
            DROPPED -> getString(R.string.dropped)
            PLAN_TO_READ -> getString(R.string.plan_to_read)
            else -> ""
        }
    }

    override fun getScoreList(): List<String> {
        val df = DecimalFormat("0.#")
        return listOf("0") + IntRange(2, 20).map { df.format(it / 2f) }
    }

    override fun indexToScore(index: Int): Float {
        return if (index > 0) (index + 1) / 2f else 0f
    }

    override fun displayScore(track: Track): String {
        val df = DecimalFormat("0.#")
        return df.format(track.score)
    }

    override fun add(track: Track): Observable<Track> {
        return api.addLibManga(track, getUserId())
    }

    override fun update(track: Track): Observable<Track> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun bind(track: Track): Observable<Track> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(query: String): Observable<List<TrackSearch>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun refresh(track: Track): Observable<Track> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun login(username: String, password: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getUserId(): String {
        return getPassword()
    }

    fun saveToken(oauth: OAuth?) {
        val json = gson.toJson(oauth)
        preferencesHelper.trackToken(this).set(json)
    }

    fun restoreToken(): OAuth? {
        return try {
            gson.fromJson(preferencesHelper.trackToken(this).get(), OAuth::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
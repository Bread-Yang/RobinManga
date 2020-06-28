package template.data.track

import android.support.annotation.CallSuper
import android.support.annotation.DrawableRes
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.OkHttpClient
import template.App
import template.data.database.models.Track
import template.data.track.model.TrackSearch
import template.network.NetworkHelper
import template.data.preference.PreferencesHelper

abstract class TrackService(val id: Int) {

    val preferencesHelper: PreferencesHelper = App.app.lazyPreferencesHelper.get()
    val networkService: NetworkHelper = App.app.lazyNetworkHelper.get()

    open val client: OkHttpClient
        get() = networkService.client

    // Name of the manga sync service to display
    abstract val name: String

    @DrawableRes
    abstract fun getLogo(): Int

    abstract fun getLogoColor(): Int

    abstract fun getStatusList(): List<Int>

    abstract fun getStatus(status: Int): String

    abstract fun getScoreList(): List<String>

    open fun indexToScore(index: Int): Float {
        return index.toFloat()
    }

    abstract fun displayScore(track: Track): String

    abstract fun add(track: Track): Observable<Track>

    abstract fun update(track: Track): Observable<Track>

    abstract fun bind(track: Track): Observable<Track>

    abstract fun search(query: String): Observable<List<TrackSearch>>

    abstract fun refresh(track: Track): Observable<Track>

    abstract fun login(username: String, password: String): Completable

    @CallSuper
    open fun logout() {
        preferencesHelper.setTrackCredentials(this, "", "")
    }

    open val isLogged: Boolean
        get() = !getUsername().isEmpty() &&
                !getPassword().isEmpty()

    fun getUsername() = preferencesHelper.trackUsername(this)

    fun getPassword() = preferencesHelper.trackPassword(this)

    fun saveCredentials(username: String, password: String) {
        preferencesHelper.setTrackCredentials(this, username, password)
    }
}

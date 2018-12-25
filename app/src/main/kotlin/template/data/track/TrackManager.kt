package template.data.track

import android.content.Context
import template.data.track.anilist.Anilist
import template.data.track.kitsu.Kitsu
import template.data.track.myanimelist.MyAnimeList

class TrackManager(private val context: Context) {

    companion object {
        const val MYANIMELIST = 1
        const val ANILIST = 2
        const val KITSU = 3
    }

    val myAnimeList = MyAnimeList(context, MYANIMELIST)

    val aniList = Anilist(context, ANILIST)

    val kitsu = Kitsu(context, KITSU)

    val services = listOf(myAnimeList, aniList, kitsu)

    fun getService(id: Int) = services.find {
        it.id == id
    }

    fun hasLoggedServices() = services.any {
        it.isLogged
    }
}

package template.data.track.myanimelist

import android.content.Context
import template.data.track.TrackService

class Myanimelist(private val context: Context, id: Int) : TrackService(id) {

    companion object {

        const val READING = 1
        const val COMPLETED = 2
        const val ON_HOLD = 3
        const val DROPPED = 4
        const val PLAY_TO_READ = 6

        const val DEFAULT_STATUS = READING
        const val DEFAULT_SCORE = 0
    }

    private val api by lazy {

    }
}
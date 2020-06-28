package template.data.preference

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.RxSharedPreferences
import template.R
import template.data.track.TrackService
import template.source.Source
import java.io.File

open class PreferencesHelper(val context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val rxPrefs = RxSharedPreferences.create(prefs)

    private val defaultDownloadsDir = Uri.fromFile(
            File(Environment.getExternalStorageDirectory().absolutePath + File.separator +
                    context.getString(R.string.app_name), "downloads"))

    private val defaultBackupDir = Uri.fromFile(
            File(Environment.getExternalStorageDirectory().absolutePath + File.separator +
                    context.getString(R.string.app_name), "backup"))

    fun startScreen() = prefs.getInt(PreferenceKeys.startScreen, 1)

    fun clear() = prefs.edit().clear().apply()

    fun theme() = prefs.getInt(PreferenceKeys.theme, 1)

    fun rotation() = rxPrefs.getInteger(PreferenceKeys.rotation, 1)

    fun pageTransitions() = rxPrefs.getBoolean(PreferenceKeys.enableTransitions, true)

    fun doubleTapAnimSpeed() = rxPrefs.getInteger(PreferenceKeys.doubleTapAnimationSpeed, 500)

    fun showPageNumber() = rxPrefs.getBoolean(PreferenceKeys.showPageNumber, true)

    fun fullscreen() = rxPrefs.getBoolean(PreferenceKeys.fullscreen, true)

    fun keepScreenOn() = rxPrefs.getBoolean(PreferenceKeys.keepScreenOn, true)

    fun customBrightness() = rxPrefs.getBoolean(PreferenceKeys.customBrightness, false)

    fun customBrightnessValue() = rxPrefs.getInteger(PreferenceKeys.customBrightnessValue, 0)

    fun colorFilter() = rxPrefs.getBoolean(PreferenceKeys.colorFilter, false)

    fun colorFilterValue() = rxPrefs.getInteger(PreferenceKeys.colorFilterValue, 0)

    fun defaultViewer() = prefs.getInt(PreferenceKeys.defaultViewer, 1)

    fun imageScaleType() = rxPrefs.getInteger(PreferenceKeys.imageScaleType, 1)

    fun imageDecoder() = rxPrefs.getInteger(PreferenceKeys.imageDecoder, 0)

    fun zoomStart() = rxPrefs.getInteger(PreferenceKeys.zoomStart, 1)

    fun readerTheme() = rxPrefs.getInteger(PreferenceKeys.readerTheme, 0)

    fun cropBorders() = rxPrefs.getBoolean(PreferenceKeys.cropBorders, false)

    fun cropBordersWebtoon() = rxPrefs.getBoolean(PreferenceKeys.cropBordersWebtoon, false)

    fun readWithTapping() = rxPrefs.getBoolean(PreferenceKeys.readWithTapping, true)

    fun readWithVolumeKeys() = rxPrefs.getBoolean(PreferenceKeys.readWithVolumeKeys, false)

    fun readWithVolumeKeysInverted() = rxPrefs.getBoolean(PreferenceKeys.readWithVolumeKeysInverted, false)

    fun portraitColumns() = rxPrefs.getInteger(PreferenceKeys.portraitColumns, 0)

    fun landscapeColumns() = rxPrefs.getInteger(PreferenceKeys.landscapeColumns, 0)

    fun updateOnlyNonCompleted() = prefs.getBoolean(PreferenceKeys.updateOnlyNonCompleted, false)

    fun autoUpdateTrack() = prefs.getBoolean(PreferenceKeys.autoUpdateTrack, true)

    fun askUpdateTrack() = prefs.getBoolean(PreferenceKeys.askUpdateTrack, false)

    fun lastUsedCatalogueSource() = rxPrefs.getLong(PreferenceKeys.lastUsedCatalogueSource, -1)

    fun lastUsedCategory() = rxPrefs.getInteger(PreferenceKeys.lastUsedCategory, 0)

    fun lastVersionCode() = rxPrefs.getInteger("last_version_code", 0)

    fun catalogueAsList() = rxPrefs.getBoolean(PreferenceKeys.catalogueAsList, false)

    fun enabledLanguages() = rxPrefs.getStringSet(PreferenceKeys.enabledLanguages, setOf("en"))

    fun sourceUsername(source: Source) = prefs.getString(PreferenceKeys.sourceUsername(source.id), "")

    fun sourcePassword(source: Source) = prefs.getString(PreferenceKeys.sourcePassword(source.id), "")

    fun setSourceCredentials(source: Source, username: String, password: String) {
        prefs.edit()
                .putString(PreferenceKeys.sourceUsername(source.id), username)
                .putString(PreferenceKeys.sourcePassword(source.id), password)
                .apply()
    }

    fun trackUsername(sync: TrackService) = prefs.getString(PreferenceKeys.trackUsername(sync.id), "")

    fun trackPassword(sync: TrackService) = prefs.getString(PreferenceKeys.trackPassword(sync.id), "")

    fun setTrackCredentials(sync: TrackService, username: String, password: String) {
        prefs.edit()
                .putString(PreferenceKeys.trackUsername(sync.id), username)
                .putString(PreferenceKeys.trackPassword(sync.id), password)
                .apply()
    }

    fun trackToken(sync: TrackService) = rxPrefs.getString(PreferenceKeys.trackToken(sync.id), "")

    fun anilistScoreType() = rxPrefs.getString("anilist_score_type", "POINT_10")

    fun backupsDirectory() = rxPrefs.getString(PreferenceKeys.backupDirectory, defaultBackupDir.toString())

    fun downloadsDirectory() = rxPrefs.getString(PreferenceKeys.downloadsDirectory, defaultDownloadsDir.toString())

    fun downloadOnlyOverWifi() = prefs.getBoolean(PreferenceKeys.downloadOnlyOverWifi, true)

    fun numberOfBackups() = rxPrefs.getInteger(PreferenceKeys.numberOfBackups, 1)

    fun backupInterval() = rxPrefs.getInteger(PreferenceKeys.backupInterval, 0)

    fun removeAfterReadSlots() = prefs.getInt(PreferenceKeys.removeAfterReadSlots, -1)

    fun removeAfterMarkedAsRead() = prefs.getBoolean(PreferenceKeys.removeAfterMarkedAsRead, false)

    fun libraryUpdateInterval() = rxPrefs.getInteger(PreferenceKeys.libraryUpdateInterval, 0)

    fun libraryUpdateRestriction() = prefs.getStringSet(PreferenceKeys.libraryUpdateRestriction, emptySet())

    fun libraryUpdateCategories() = rxPrefs.getStringSet(PreferenceKeys.libraryUpdateCategories, emptySet())

    fun libraryAsList() = rxPrefs.getBoolean(PreferenceKeys.libraryAsList, false)

    fun downloadBadge() = rxPrefs.getBoolean(PreferenceKeys.downloadBadge, false)

    fun filterDownloaded() = rxPrefs.getBoolean(PreferenceKeys.filterDownloaded, false)

    fun filterUnread() = rxPrefs.getBoolean(PreferenceKeys.filterUnread, false)

    fun filterCompleted() = rxPrefs.getBoolean(PreferenceKeys.filterCompleted, false)

    fun librarySortingMode() = rxPrefs.getInteger(PreferenceKeys.librarySortingMode, 0)

    fun librarySortingAscending() = rxPrefs.getBoolean("library_sorting_ascending", true)

    fun automaticUpdates() = prefs.getBoolean(PreferenceKeys.automaticUpdates, false)

    fun hiddenCatalogues() = rxPrefs.getStringSet("hidden_catalogues", emptySet())

    fun downloadNew() = rxPrefs.getBoolean(PreferenceKeys.downloadNew, false)

    fun downloadNewCategories() = rxPrefs.getStringSet(PreferenceKeys.downloadNewCategories, emptySet())

    fun lang() = prefs.getString(PreferenceKeys.lang, "")

    fun defaultCategory() = prefs.getInt(PreferenceKeys.defaultCategory, -1)

    fun migrateFlags() = rxPrefs.getInteger("migrate_flags", Int.MAX_VALUE)

    fun trustedSignatures() = rxPrefs.getStringSet("trusted_signatures", emptySet())
}
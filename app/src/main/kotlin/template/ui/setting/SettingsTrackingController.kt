package template.ui.setting

import android.app.Activity
import android.content.Intent
import android.support.customtabs.CustomTabsIntent
import android.support.v7.preference.PreferenceScreen
import template.App
import template.R
import template.data.preference.PreferenceKeys
import template.data.track.TrackManager
import template.data.track.TrackService
import template.data.track.anilist.AnilistApi
import template.extensions.getResourceColor
import template.widget.preference.LoginPreference
import template.widget.preference.TrackLoginDialog

class SettingsTrackingController : SettingsController(), TrackLoginDialog.Listener {

    private val trackManager: TrackManager = App.app.lazyTrackManager.get()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.pref_category_tracking

        switchPreference {
            key = PreferenceKeys.autoUpdateTrack
            titleRes = R.string.pref_auto_update_manga_sync
            defaultValue = true
        }

        switchPreference {
            key = PreferenceKeys.askUpdateTrack
            titleRes = R.string.pref_ask_update_manga_sync
            defaultValue = false
        }.apply {
            dependency = PreferenceKeys.autoUpdateTrack // the preference needs to be attached.
        }

        preferenceCategory {
            titleRes = R.string.services

            trackPreference(trackManager.myAnimeList) {
                onClick {
                    val dialog = TrackLoginDialog(trackManager.myAnimeList)
                    dialog.targetController = this@SettingsTrackingController
                    dialog.showDialog(router)
                }
            }

            trackPreference(trackManager.aniList) {
                onClick {
                    // Chrome Custom Tabs
                    val tabsIntent = CustomTabsIntent.Builder()
                            .setToolbarColor(context.getResourceColor(R.attr.colorPrimary))
                            .build()
                    tabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    tabsIntent.launchUrl(activity, AnilistApi.authUrl())
                }
            }

            trackPreference(trackManager.kitsu) {
                onClick {
                    val dialog = TrackLoginDialog(trackManager.kitsu)
                    dialog.targetController = this@SettingsTrackingController
                    dialog.showDialog(router)
                }
            }
        }
    }

    inline fun PreferenceScreen.trackPreference(
            service: TrackService,
            block: (@DSL LoginPreference).() -> Unit
    ): LoginPreference {
        return initThenAdd(LoginPreference(context).apply {
            key = PreferenceKeys.trackUsername(service.id)
            title = service.name
        }, block)
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        // Manually refresh anilist holder
        updatePreference(trackManager.aniList.id)
    }

    private fun updatePreference(id: Int) {
        val pref = findPreference(PreferenceKeys.trackUsername(id)) as? LoginPreference
        pref?.notifyChanged()
    }

    override fun trackDialogClosed(service: TrackService) {
        updatePreference(service.id)
    }
}
package template.ui.setting

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.support.v7.preference.PreferenceScreen
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.pref_library_columns.view.*
import template.App
import template.R
import template.data.database.DatabaseHelper
import template.data.library.LibraryUpdateJob
import template.data.preference.PreferenceKeys
import template.data.preference.PreferencesHelper
import template.extensions.getOrDefault
import template.ui.base.controller.DialogController
import template.utils.LocaleHelper

class SettingsGeneralController : SettingsController() {

    private val databaseHelper: DatabaseHelper = App.app.lazyDatabaseHelper.get()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.pref_category_general

        listPreference {
            key = PreferenceKeys.lang
            titleRes = R.string.pref_language
            entryValues = arrayOf("", "ar", "bg", "bn", "de", "en-US", "en-GB", "es", "fr", "hi",
                    "hu", "in", "it", "ja", "ko", "lv", "ms", "nl", "pl", "pt", "pt-BR", "ro",
                    "ru", "vi")
            entries = entryValues.map { value ->
                val locale = LocaleHelper.getLocaleFromString(value.toString())
                locale?.getDisplayName(locale)?.capitalize()
                        ?: context.getString(R.string.system_default)
            }.toTypedArray()
            defaultValue = ""
            summary = "%s"

            onChange { newValue ->
                val activity = activity ?: return@onChange false
                val app = activity.application
                LocaleHelper.changeLocale(newValue.toString())
                LocaleHelper.updateConfiguration(app, app.resources.configuration)
                activity.recreate()
                true
            }
        }

        intListPreference {
            key = PreferenceKeys.theme
            titleRes = R.string.pref_theme
            entriesRes = arrayOf(R.string.light_theme, R.string.dark_theme, R.string.amoled_theme)
            entryValues = arrayOf("1", "2", "3")
            defaultValue = "1"
            summary = "%s"

            onChange {
                activity?.recreate()
                true
            }
        }

        preference {
            titleRes = R.string.pref_library_columns

            onClick {
                LibraryColumnsDialog().showDialog(router)
            }

            fun getColumnValue(value: Int): String {
                return if (value == 0)
                    context.getString(R.string.default_columns)
                else
                    value.toString()
            }

            // combineLatest RxJava2 用法 : https://dev.to/amay077/rxjava-combinelatest-quick-example-e8b
            Observable
                    .combineLatest<Int, Int, Pair<Int, Int>>(
                            preferencesHelper.portraitColumns().asObservable(),
                            preferencesHelper.landscapeColumns().asObservable(),
                            BiFunction { portraitCols, landscapeCols ->
                                Pair(portraitCols, landscapeCols)
                            }
                    )
                    .subscribeUntilDestroy { (portraitCols, landscapeCols) ->
                        val portrait = getColumnValue(portraitCols)
                        val landscape = getColumnValue(landscapeCols)
                        summary = "${context.getString(R.string.portrait)}: $portrait, " +
                                "${context.getString(R.string.landscape)}: $landscape"
                    }
        }

        intListPreference {
            key = PreferenceKeys.startScreen
            titleRes = R.string.pref_start_screen
            entriesRes = arrayOf(R.string.label_library, R.string.label_recent_manga,
                    R.string.label_recent_updates)
            entryValues = arrayOf("1", "2", "3")
            defaultValue = "1"
            summary = "%s"
        }

        intListPreference {
            key = PreferenceKeys.libraryUpdateInterval
            titleRes = R.string.pref_library_update_interval
            entriesRes = arrayOf(R.string.update_never, R.string.update_1hour,
                    R.string.update_2hour, R.string.update_3hour, R.string.update_6hour,
                    R.string.update_12hour, R.string.update_24hour, R.string.update_48hour)
            entryValues = arrayOf("0", "1", "2", "3", "6", "12", "24", "48")
            defaultValue = "0"
            summary = "%s"

            onChange { newValue ->
                // Always cancel the previous task, it seems that sometimes they are not updated.
                LibraryUpdateJob.cancelTask()

                val interval = (newValue as String).toInt()
                if (interval > 0) {
                    LibraryUpdateJob.setupTask(interval)
                }
                true
            }
        }

        multiSelectListPreference {
            key = PreferenceKeys.libraryUpdateRestriction
            titleRes = R.string.pref_library_update_restriction
            entriesRes = arrayOf(R.string.wifi, R.string.charging)
            entryValues = arrayOf("wifi", "ac")
            summaryRes = R.string.pref_library_update_restriction_summary

            preferencesHelper.libraryUpdateInterval().asObservable()
                    .subscribeUntilDestroy { isVisible = it > 0 }

            onChange {
                // Post to event looper to allow the preference to be updated.
                Handler().post { LibraryUpdateJob.setupTask() }
                true
            }
        }

        switchPreference {
            key = PreferenceKeys.updateOnlyNonCompleted
            titleRes = R.string.pref_update_only_non_completed
            defaultValue = false
        }

        val dbCategories = databaseHelper.getCategories().executeAsBlocking()!!

        multiSelectListPreference {
            key = PreferenceKeys.libraryUpdateCategories
            titleRes = R.string.pref_library_update_categories
            entries = dbCategories.map { it.name }.toTypedArray()
            entryValues = dbCategories.map { it.id.toString() }.toTypedArray()

            preferencesHelper.libraryUpdateCategories().asObservable()
                    .subscribeUntilDestroy {
                        val selectedCategories = it
                                .mapNotNull { id -> dbCategories.find { it.id == id.toInt() } }
                                .sortedBy { it.order }

                        summary = if (selectedCategories.isEmpty())
                            context.getString(R.string.all)
                        else
                            selectedCategories.joinToString { it.name }
                    }
        }

        intListPreference {
            key = PreferenceKeys.defaultCategory
            titleRes = R.string.default_category

            val selectedCategory = dbCategories.find { it.id == preferencesHelper.defaultCategory() }
            entries = arrayOf(context.getString(R.string.default_category_summary)) +
                    dbCategories?.map { it.name }.toTypedArray()
            entryValues = arrayOf("-1") + dbCategories?.map { it.id.toString() }.toTypedArray()
            defaultValue = "-1"
            summary = selectedCategory?.name ?: context.getString(R.string.default_category_summary)

            onChange { newValue ->
                summary = dbCategories.find {
                    it.id == (newValue as String).toInt()
                }?.name ?: context.getString(R.string.default_category_summary)
                true
            }
        }
    }

    class LibraryColumnsDialog : DialogController() {

        private val preferences: PreferencesHelper = App.app.lazyPreferencesHelper.get()

        private var portrait = preferences.portraitColumns().getOrDefault()
        private var landscape = preferences.landscapeColumns().getOrDefault()

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            val dialog = MaterialDialog.Builder(activity!!)
                    .title(R.string.pref_library_columns)
                    .customView(R.layout.pref_library_columns, false)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive { dialog, which ->
                        preferences.portraitColumns().set(portrait)
                        preferences.landscapeColumns().set(landscape)
                    }
                    .build()

            onViewCreated(dialog.view)
            return dialog
        }

        fun onViewCreated(view: View) {
            with(view.portrait_columns) {
                displayedValues = arrayOf(context.getString(R.string.default_columns)) +
                        IntRange(1, 10).map(Int::toString)
                value = portrait

                setOnValueChangedListener { _, _, newValue ->
                    portrait = newValue
                }
            }
            with(view.landscape_columns) {
                displayedValues = arrayOf(context.getString(R.string.default_columns)) +
                        IntRange(1, 10).map(Int::toString)
                value = landscape

                setOnValueChangedListener { _, _, newValue ->
                    landscape = newValue
                }
            }
        }
    }
}

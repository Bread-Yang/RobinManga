package template.ui.setting

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.preference.PreferenceScreen
import com.afollestad.materialdialogs.MaterialDialog
import com.hippo.unifile.UniFile
import template.App
import template.R
import template.data.database.DatabaseHelper
import template.data.preference.PreferenceKeys
import template.data.preference.PreferencesHelper
import template.extensions.getFilePicker
import template.extensions.getOrDefault
import template.ui.base.controller.DialogController
import template.utils.DiskUtil
import java.io.File

class SettingsDownloadController : SettingsController() {

    private companion object {
        const val DOWNLOAD_DIR_PRE_L = 103
        const val DOWNLOAD_DIR_L = 104
    }

    private val databaseHelper: DatabaseHelper = App.app.lazyDatabaseHelper.get()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.pref_category_downloads

        preference {
            key = PreferenceKeys.downloadsDirectory
            titleRes = R.string.pref_download_directory
            onClick {
                val ctrl = DownloadDirectoriesDialog()
                ctrl.targetController = this@SettingsDownloadController
                ctrl.showDialog(router)
            }

            preferencesHelper.downloadsDirectory()
                    .asObservable()
                    .subscribeUntilDestroy { path ->
                        val dir = UniFile.fromUri(context, Uri.parse(path))
                        summary = dir.filePath ?: path

                        // Don't display downloaded chapters in gallery apps creating .nomedia
                        if (dir != null && dir.exists()) {
                            val nomedia = dir.findFile(".nomedia")
                            if (nomedia == null) {
                                dir.createFile(".nomedia")
                                applicationContext?.let { DiskUtil.scanMedia(it, dir.uri) }
                            }
                        }
                    }
        }

        switchPreference {
            key = PreferenceKeys.downloadOnlyOverWifi
            titleRes = R.string.pref_download_only_over_wifi
            defaultValue = true
        }

        preferenceCategory {
            titleRes = R.string.pref_remove_after_read

            switchPreference {
                key = PreferenceKeys.removeAfterMarkedAsRead
                titleRes = R.string.pref_remove_after_marked_as_read
                defaultValue = true
            }

            intListPreference {
                key = PreferenceKeys.removeAfterReadSlots
                titleRes = R.string.pref_remove_after_read
                entriesRes = arrayOf(R.string.disabled, R.string.last_read_chapter,
                        R.string.second_to_last, R.string.third_to_last, R.string.fourth_to_last,
                        R.string.fifth_to_last)
                entryValues = arrayOf("-1", "0", "1", "2", "3", "4")
                defaultValue = "-1"
                summary = "%s"
            }
        }

        val dbCategories = databaseHelper.getCategories().executeAsBlocking()!!

        preferenceCategory {
            titleRes = R.string.pref_download_new

            switchPreference {
                key = PreferenceKeys.downloadNew
                titleRes = R.string.pref_download_new
                defaultValue = false
            }

            multiSelectListPreference {
                key = PreferenceKeys.downloadNewCategories
                titleRes = R.string.pref_download_new_categories
                entries = dbCategories.map { it.name }.toTypedArray()
                entryValues = dbCategories.map { it.id.toString() }.toTypedArray()

                preferencesHelper
                        .downloadNew()
                        .asObservable()
                        .subscribeUntilDestroy {
                            isVisible = it
                        }

                preferencesHelper
                        .downloadNewCategories()
                        .asObservable()
                        .subscribeUntilDestroy {
                            val selectedCategories = it
                                    .mapNotNull { id: String ->
                                        dbCategories.find {
                                            it.id == id.toInt()
                                        }
                                    }
                                    .sortedBy {
                                        it.order
                                    }

                            summary = if (selectedCategories.isEmpty())
                                resources?.getString(R.string.all)
                            else
                                selectedCategories.joinToString { it.name }
                            //  joinToString example :
                            //  val numbers = listOf(1, 2, 3, 4, 5, 6)
                            //  println(numbers.joinToString()) // 1, 2, 3, 4, 5, 6
                            //  println(numbers.joinToString(prefix = "[", postfix = "]")) // [1, 2, 3, 4, 5, 6]
                            //  println(numbers.joinToString(prefix = "<", postfix = ">", separator = "•")) // <1•2•3•4•5•6>
                            //
                            //  val chars = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q')
                            //  println(chars.joinToString(limit = 5, truncated = "...!") { it.toUpperCase().toString() }) // A, B, C, D, E, ...!
                        }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            DOWNLOAD_DIR_PRE_L -> if (data != null && resultCode == Activity.RESULT_OK) {
                val uri = Uri.fromFile(File(data.data.path))
                preferencesHelper.downloadsDirectory().set(uri.toString())
            }
            DOWNLOAD_DIR_L -> if (data != null && resultCode == Activity.RESULT_OK) {
                val context = applicationContext ?: return
                val uri = data.data
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                // 解释 : http://www.andreamaglie.com/2015/access-storage-framework-uri-permission/
                @Suppress("NewApi")
                context.contentResolver.takePersistableUriPermission(uri, flags)

                val file = UniFile.fromUri(context, uri)
                preferencesHelper.downloadsDirectory().set(file.uri.toString())
            }
        }
    }

    fun predefinedDirectorySelected(selectedDir: String) {
        val path = Uri.fromFile(File(selectedDir))
        preferencesHelper.downloadsDirectory().set(path.toString())
    }

    fun customDirectorySelected(currentDir: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(preferencesHelper.context.getFilePicker(currentDir), DOWNLOAD_DIR_PRE_L)
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            try {
                startActivityForResult(intent, DOWNLOAD_DIR_L)
            } catch (e: ActivityNotFoundException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(preferencesHelper.context.getFilePicker(currentDir), DOWNLOAD_DIR_L)
                }
            }
        }
    }

    class DownloadDirectoriesDialog : DialogController() {

        private val preferences: PreferencesHelper = App.app.lazyPreferencesHelper.get()

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            val activity = activity!!
            val currentDir = preferences.downloadsDirectory().getOrDefault()
            val externalDirs = getExternalDirs() + File(activity.getString(R.string.custom_dir))
            val selectedIndex = externalDirs.map(File::toString).indexOfFirst { it in currentDir }

            return MaterialDialog.Builder(activity)
                    .items(externalDirs)
                    .itemsCallbackSingleChoice(selectedIndex) { _, _, which, text ->
                        val target = targetController as? SettingsDownloadController
                        if (which == externalDirs.lastIndex) {
                            target?.customDirectorySelected(currentDir)
                        } else {
                            target?.predefinedDirectorySelected(text.toString())
                        }
                        true
                    }
                    .build()
        }

        private fun getExternalDirs(): List<File> {
            val defaultDir = Environment.getExternalStorageDirectory().absolutePath +
                    File.separator + resources?.getString(R.string.app_name) +
                    File.separator + "downloads"

            return mutableListOf(File(defaultDir)) +
                    ContextCompat.getExternalFilesDirs(activity!!, "").filterNotNull()
        }
    }
}

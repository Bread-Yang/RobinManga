package template.ui.setting

import android.support.v7.preference.PreferenceScreen
import template.R

class SettingsBackupController : SettingsController() {

    private companion object {
        const val CODE_BACKUP_CREATE = 501
        const val CODE_BACKUP_RESTORE = 502
        const val CODE_BACKUP_DIR = 503

        const val TAG_CREATING_BACKUP_DIALOG = "CreatingBackupDialog"
        const val TAG_RESTORING_BACKUP_DIALOG = "RestoringBackupDialog"
    }

    /**
     * Flags containing information of what to backup.
     */
    private var backupFlags = 0

//    private val receiver = BackupBroadcastReceiver()

    init {

    }

    override fun setupPreferenceScreen(screen: PreferenceScreen) = with(screen) {
        titleRes = R.string.backup

        preference {
            titleRes = R.string.pref_create_backup
            summaryRes = R.string.pref_create_backup_summ

            onClick {
            }
        }
    }

//    inner class BackupBroadcastReceiver : BroadcastReceiver() {
//
//        override fun onReceive(context: Context?, intent: Intent) {
//            when (intent.getStringExtra(BackupConst.ACTION)) {
//                BackupConst.ACTION_BACKUP_COMPLETED_DIALOG -> {
//                    router.popControllerWithTag(TAG_CREATING_BACKUP_DIALOG)
//                    val uri = Uri.parse(intent.getStringExtra(BackupConst.EXTRA_URI))
//                    CreatedBackupDialog(uri).showDialog(router)
//                }
//                BackupConst.ACTION_SET_PROGRESS_DIALOG -> {
//                    val progress = intent.getIntExtra(BackupConst.EXTRA_PROGRESS, 0)
//                    val amount = intent.getIntExtra(BackupConst.EXTRA_AMOUNT, 0)
//                    val content = intent.getStringExtra(BackupConst.EXTRA_CONTENT)
//                    (router.getControllerWithTag(TAG_RESTORING_BACKUP_DIALOG)
//                            as? RestoringBackupDialog)?.updateProgress(content, progress, amount)
//                }
//            }
//        }
//    }
//
//    class CreatedBackupDialog(bundle: Bundle? = null) : DialogController(bundle) {
//        constructor(uri: Uri) : this(Bundle().apply {
//            putParcelable(KEY_URI, uri)
//        })
//
//        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
//            val activity = activity!!
//            val unifile = UniFile.fromUri(activity, args.getParcelable(KEY_URI))
//            return MaterialDialog.Builder(activity)
//                    .title(R.string.backup_created)
//                    .content(activity.getString(R.string.file_saved, unifile.filePath))
//                    .positiveText(R.string.action_close)
//                    .negativeText(R.string.action_export)
//                    .onNegative { _, _ ->
//                        val sendIntent = Intent(Intent.ACTION_SEND)
//                        sendIntent.type = "application/json"
//                        sendIntent.putExtra(Intent.EXTRA_STREAM, unifile.uri)
//                        startActivity(Intent.createChooser(sendIntent, ""))
//                    }
//                    .build()
//
//        }
//
//        private companion object {
//            const val KEY_URI = "BackupCreatedDialog.uri"
//        }
//    }
//
//    class RestoringBackupDialog : DialogController() {
//        private var materialDialog: MaterialDialog? = null
//
//        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
//            return MaterialDialog.Builder(activity!!)
//                    .title(R.string.backup)
//                    .content(R.string.restoring_backup)
//                    .progress(false, 100, true)
//                    .cancelable(false)
//                    .negativeText(R.string.action_stop)
//                    .onNegative { _, _ ->
//                        applicationContext?.let { BackupRestoreService.stop(it) }
//                    }
//                    .build()
//                    .also { materialDialog = it }
//        }
//
//        override fun onDestroyView(view: View) {
//            super.onDestroyView(view)
//            materialDialog = null
//        }
//
//        override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//            super.onRestoreInstanceState(savedInstanceState)
//            router.popController(this)
//        }
//
//        fun updateProgress(content: String?, progress: Int, amount: Int) {
//            val dialog = materialDialog ?: return
//            dialog.setContent(content)
//            dialog.setProgress(progress)
//            dialog.maxProgress = amount
//        }
//    }
}
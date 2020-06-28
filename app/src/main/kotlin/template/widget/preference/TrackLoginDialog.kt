package template.widget.preference

import android.os.Bundle
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.pref_account_login.view.*
import template.App
import template.R
import template.data.track.TrackService
import template.extensions.toast

class TrackLoginDialog(bundle: Bundle? = null) : LoginDialogPreference(bundle) {

    private val service = App.app.lazyTrackManager.get().getService(args.getInt("key"))!!

    constructor(service: TrackService) : this(Bundle().apply { putInt("key", service.id) })

    override fun setCredentialsOnView(view: View) = with(view) {
        dialog_title.text = context.getString(R.string.login_title, service.name)
        username.setText(service.getUsername())
        password.setText(service.getPassword())
    }

    override fun checkLogin() {
        requestDisposable?.dispose()

        v?.apply {
            if (username.text.isEmpty() || password.text.isEmpty())
                return

            login.progress = 1
            val user = username.text.toString()
            val pass = password.text.toString()

            requestDisposable = service.login(user, pass)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        dialog?.dismiss()
                        context.toast(R.string.login_success)
                    }, {
                        login.progress = -1
                        login.setText(R.string.unknown_error)
                        it.message?.let {
                            context.toast(it)
                        }
                    })
        }
    }

    override fun onDialogClosed() {
        super.onDialogClosed()
        (targetController as? Listener)?.trackDialogClosed(service)
    }

    interface Listener {
        fun trackDialogClosed(service: TrackService)
    }
}
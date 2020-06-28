package template.widget.preference

import android.app.Dialog
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import com.dd.processbutton.iml.ActionProcessButton
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.pref_account_login.view.*
import template.App
import template.R
import template.data.preference.PreferencesHelper
import template.ui.base.controller.DialogController
import template.widget.SimpleTextWatcher

abstract class LoginDialogPreference(bundle: Bundle? = null) : DialogController(bundle) {

    var v: View? = null
        private set

    val preferences: PreferencesHelper = App.app.lazyPreferencesHelper.get()

    var requestDisposable: Disposable? = null

    override fun onCreateDialog(savedViewState: Bundle?): Dialog {
        val dialog = MaterialDialog.Builder(activity!!)
                .customView(R.layout.pref_account_login, false)
                .negativeText(android.R.string.cancel)
                .build()

        onViewCreated(dialog.view)

        return dialog
    }

    fun onViewCreated(view: View) {
        v = view.apply {
            show_password.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                    password.transformationMethod = null
                else
                    password.transformationMethod = PasswordTransformationMethod()  // 明文显示
            }

            login.setMode(ActionProcessButton.Mode.ENDLESS)
            login.setOnClickListener { checkLogin() }

            setCredentialsOnView(this)

            show_password.isEnabled = password.text.isNullOrEmpty()

            password.addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isEmpty()) {
                        show_password.isEnabled = true
                    }
                }
            })
        }
    }

    override fun onChangeStarted(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        super.onChangeStarted(changeHandler, changeType)
        if (!changeType.isEnter) {
            onDialogClosed()
        }
    }

    open fun onDialogClosed() {
        requestDisposable?.dispose()
    }

    protected abstract fun checkLogin()

    protected abstract fun setCredentialsOnView(view: View)
}
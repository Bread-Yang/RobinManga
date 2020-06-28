package template.ui.setting

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceController
import androidx.preference.PreferenceScreen
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeType
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import template.App
import template.R
import template.data.preference.PreferencesHelper
import template.ui.common.mvp.controller.BaseController

abstract class SettingsController : PreferenceController() {

    val preferencesHelper: PreferencesHelper = App.app.lazyPreferencesHelper.get()

    var untilDestroyDisposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        if (untilDestroyDisposables.isDisposed) {
            untilDestroyDisposables = CompositeDisposable()
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        untilDestroyDisposables.dispose()
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        val screen = preferenceManager.createPreferenceScreen(getThemedContext())
        preferenceScreen = screen
        setupPreferenceScreen(screen)
    }

    abstract fun setupPreferenceScreen(screen: PreferenceScreen): Any?

    private fun getThemedContext(): Context {
        val tv = TypedValue()
        activity!!.theme.resolveAttribute(R.attr.preferenceTheme, tv, true)
        return ContextThemeWrapper(activity, tv.resourceId)
    }

    open fun getTitle(): String? {
        return preferenceScreen?.title?.toString()
    }

    fun setTitle() {
        var parentController = parentController
        while (parentController != null) {
            if (parentController is BaseController && parentController.getTitle() != null) {
                return
            }
            parentController = parentController.parentController
        }

        (activity as? AppCompatActivity)?.supportActionBar?.title = getTitle()
    }

    override fun onChangeStarted(changeHandler: ControllerChangeHandler, changeType: ControllerChangeType) {
        if (changeType.isEnter) {
            setTitle()
        }
        super.onChangeStarted(changeHandler, changeType)
    }

    fun <T> Observable<T>.subscribeUntilDestroy(): Disposable {
        return subscribe().also { untilDestroyDisposables.add(it) }
    }

    fun <T> Observable<T>.subscribeUntilDestroy(onNext: (T) -> Unit): Disposable {
        return subscribe(onNext).also { untilDestroyDisposables.add(it) }
    }
}

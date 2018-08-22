package template.ui.common.mvp.controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RestoreViewOnCreateController
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.*
import template.ui.common.annotation.Layout

/**
 * Created by Robin Yeung on 8/22/18.
 */
abstract class BaseController(bundle: Bundle? = null) : RestoreViewOnCreateController(bundle), LayoutContainer {

    override val containerView: View?
        get() = view

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(layout(), container, false)
    }

    override fun onDestroyView(view: View) {
        super.onDestroyView(view)
        clearFindViewByIdCache()
    }

    private fun layout(): Int {
        this.javaClass.kotlin.annotations.forEach {
            if (it is Layout)
                return it.layoutRes
        }
        throw IllegalArgumentException("You should specify Layout annotation")
    }
}
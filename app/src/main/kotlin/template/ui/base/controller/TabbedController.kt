package template.ui.base.controller

import android.support.design.widget.TabLayout

/**
 * Created by Robin Yeung on 9/6/18.
 */
interface TabbedController {

    fun configureTabs(tabs: TabLayout)

    fun cleanupTabs(tabs: TabLayout)
}
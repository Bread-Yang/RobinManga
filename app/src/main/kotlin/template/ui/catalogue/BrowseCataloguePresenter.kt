package template.ui.catalogue

import template.source.SourceManager
import template.ui.common.mvp.BasePresenter
import template.utils.preference.PreferencesHelper
import javax.inject.Inject

/**
 * Created by Robin Yeung on 8/22/18.
 */
//class BrowseCataloguePresenter @Inject constructor(
//        sourceId: Long,
//        preferences: Lazy<PreferencesHelper>
//) : BasePresenter<BrowseCatalogueController>() {
//
//}
class BrowseCataloguePresenter : BasePresenter<BrowseCatalogueController>() {

    @Inject
    lateinit var preferences: PreferencesHelper

    @Inject
    lateinit var sourceManager: SourceManager

}
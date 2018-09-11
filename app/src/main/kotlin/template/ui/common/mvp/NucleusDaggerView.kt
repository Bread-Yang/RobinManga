package template.ui.common.mvp

/**
 * Created by Robin Yeung on 9/10/18.
 */
interface NucleusDaggerView {
    /**
     * 创建Presenter的时候调用,用于method Injection，在[NucleusConductorDelegate.getPresenter]之后调用，
     * view(Activity、Controller)生命周期内只调用一次,也就是就算view(Activity、Controller)创建之后，
     * rotate screen，initPresenterOnce也不会被调用
     */
    fun initPresenterOnce()
}
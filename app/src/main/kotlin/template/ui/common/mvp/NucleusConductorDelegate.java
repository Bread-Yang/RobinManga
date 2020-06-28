package template.ui.common.mvp;

/**
 * Created by Robin Yeung on 9/6/18.
 */

import android.os.Bundle;
import androidx.annotation.Nullable;

import nucleus5.factory.PresenterFactory;
import nucleus5.factory.PresenterStorage;
import nucleus5.presenter.Presenter;
import nucleus5.view.ViewWithPresenter;

/**
 * This class adopts a View lifecycle to the Presenter`s lifecycle.
 *
 * @param <P> a type of the presenter.
 */
public final class NucleusConductorDelegate<P extends Presenter> {

    private static final String PRESENTER_KEY = "presenter";
    private static final String PRESENTER_ID_KEY = "presenter_id";

    @Nullable
    private PresenterFactory<P> presenterFactory;
    @Nullable
    private NucleusDaggerView nucleusDaggerView;
    @Nullable
    private P presenter;
    @Nullable
    private Bundle bundle;

    private boolean presenterHasView;

    public NucleusConductorDelegate(@Nullable PresenterFactory<P> presenterFactory, @Nullable NucleusDaggerView nucleusDaggerView) {
        this.presenterFactory = presenterFactory;
        this.nucleusDaggerView = nucleusDaggerView;
    }

    /**
     * {@link ViewWithPresenter#getPresenterFactory()}
     */
    @Nullable
    public PresenterFactory<P> getPresenterFactory() {
        return presenterFactory;
    }

    /**
     * {@link ViewWithPresenter#setPresenterFactory(PresenterFactory)}
     */
    public void setPresenterFactory(@Nullable PresenterFactory<P> presenterFactory) {
        if (presenter != null)
            throw new IllegalArgumentException("setPresenterFactory() should be called before onResume()");
        this.presenterFactory = presenterFactory;
    }

    /**
     * {@link ViewWithPresenter#getPresenter()}
     */
    public P getPresenter() {
        if (presenterFactory != null) {
            if (presenter == null && bundle != null)
                presenter = PresenterStorage.INSTANCE.getPresenter(bundle.getString(PRESENTER_ID_KEY));

            if (presenter == null) {
                presenter = presenterFactory.createPresenter();
                if (nucleusDaggerView != null) {
                    nucleusDaggerView.initPresenterOnce();
                }
                PresenterStorage.INSTANCE.add(presenter);
                presenter.create(bundle == null ? null : bundle.getBundle(PRESENTER_KEY));
            }
            bundle = null;
        }
        return presenter;
    }

    /**
     * {@link android.app.Activity#onSaveInstanceState(Bundle)}, {@link android.app.Fragment#onSaveInstanceState(Bundle)}, {@link android.view.View#onSaveInstanceState()}.
     */
    public Bundle onSaveInstanceState() {
        Bundle bundle = new Bundle();
        getPresenter();
        if (presenter != null) {
            Bundle presenterBundle = new Bundle();
            presenter.save(presenterBundle);
            bundle.putBundle(PRESENTER_KEY, presenterBundle);
            bundle.putString(PRESENTER_ID_KEY, PresenterStorage.INSTANCE.getId(presenter));
        }
        return bundle;
    }

    /**
     * {@link android.app.Activity#onCreate(Bundle)}, {@link android.app.Fragment#onCreate(Bundle)}, {@link android.view.View#onSaveInstanceState()}.
     */
    public void onRestoreInstanceState(Bundle presenterState) {
//        if (presenter != null)
//            throw new IllegalArgumentException("onRestoreInstanceState() should be called before onResume()");
        this.bundle = ParcelFn.unmarshall(ParcelFn.marshall(presenterState));
    }

    /**
     * {@link android.app.Activity#onResume()},
     * {@link android.app.Fragment#onResume()},
     * {@link android.view.View#onAttachedToWindow()}
     */
    public void onResume(Object view) {
        getPresenter();
        if (presenter != null && !presenterHasView) {
            //noinspection unchecked
            presenter.takeView(view);
            presenterHasView = true;
        }
    }

    /**
     * {@link android.app.Activity#onDestroy()},
     * {@link android.app.Fragment#onDestroyView()},
     * {@link android.view.View#onDetachedFromWindow()}
     */
    public void onDropView() {
        if (presenter != null && presenterHasView) {
            presenter.dropView();
            presenterHasView = false;
        }
    }

    /**
     * {@link android.app.Activity#onDestroy()},
     * {@link android.app.Fragment#onDestroy()},
     * {@link android.view.View#onDetachedFromWindow()}
     */
    public void onDestroy(boolean isFinal) {
        if (presenter != null && isFinal) {
            presenter.destroy();
            presenter = null;
        }
    }
}

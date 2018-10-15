package template.ui.main

import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Created by Robin Yeung on 9/28/18.
 */
class MainPresenterTest {

    @Test
    fun testPresenter() {

        val presenter = MainPresenter()

        val activity = mock(MainActivity::class.java)
        presenter.takeView(activity)

//        val sharePreferences = mock(SharedPreferences::class.java)
//        `when`(preferences.readWithVolumeKeys()).thenReturn(null)

//        presenter.preferencesHelper = preferences

//        presenter.preferencesHelper.readWithVolumeKeys()

//        verify(presenter.preferencesHelper).readWithVolumeKeys()

        presenter.testUnitTest()
        verify(activity).callByPresenter()
    }
}

package template.ui.reader.viewer.pager;

import androidx.viewpager.widget.PagerAdapter;
import android.view.ViewGroup;

import io.reactivex.functions.Consumer;

public interface Pager {

    void setId(int id);
    void setLayoutParams(ViewGroup.LayoutParams layoutParams);

    void setOffscreenPageLimit(int limit);

    int getCurrentItem();
    void setCurrentItem(int item, boolean smoothScroll);

    int getWidth();
    int getHeight();

    abstract PagerAdapter getAdapter();
    void setAdapter(PagerAdapter adapter);

    void setOnChapterBoundariesOutListener(OnChapterBoundariesOutListener listener);

    void setOnPageChangeListener(Consumer<Integer> onPageChanged); // kotlin : fun setOnPageChangeListener(onPageChanged: (Int) -> Unit)
    void clearOnPageChangeListeners();
}

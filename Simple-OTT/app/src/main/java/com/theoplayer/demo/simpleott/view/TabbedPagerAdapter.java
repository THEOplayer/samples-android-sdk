package com.theoplayer.demo.simpleott.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Supplier;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabbedPagerAdapter extends PagerAdapter {

    private final Context context;
    private List<Tab> tabs;

    public TabbedPagerAdapter(Context context) {
        this.context = context;
        this.tabs = new ArrayList<>();
    }

    /**
     * Allows to add new tab to the tabbed view pager.
     *
     * @param titleResId - string resource ID of tab title
     * @param viewBinder - supplier that binds and inflates tab view.
     */
    public void addTab(@StringRes int titleResId, Supplier<View> viewBinder) {
        tabs.add(new Tab(titleResId, viewBinder));
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = tabs.get(position).viewBinder.get();
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(tabs.get(position).titleResId);
    }

    private static class Tab {

        @StringRes
        private int titleResId;
        private Supplier<View> viewBinder;

        Tab(@StringRes int titleResId, Supplier<View> viewBinder) {
            this.titleResId = titleResId;
            this.viewBinder = viewBinder;
        }
    }
}

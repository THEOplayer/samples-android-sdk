package com.theoplayer.demo.remotejson;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.viewpager.widget.PagerAdapter;

class TabbedPagerAdapter extends PagerAdapter {

    private Tab[] tabs = new Tab[]{
            new Tab(R.string.tabTime, R.layout.time_tab_layout),
            new Tab(R.string.tabTracks, R.layout.tracks_tab_layout),
            new Tab(R.string.tabState, R.layout.state_tab_layout),
            new Tab(R.string.tabAds, R.layout.ads_tab_layout)
    };

    private final Context context;

    TabbedPagerAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(tabs[position].layoutResId, collection, false);
        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(tabs[position].titleResId);
    }

    private static class Tab {

        @StringRes
        private int titleResId;
        @LayoutRes
        private int layoutResId;

        Tab(@StringRes int titleResId, @LayoutRes int layoutResId) {
            this.titleResId = titleResId;
            this.layoutResId = layoutResId;
        }
    }
}



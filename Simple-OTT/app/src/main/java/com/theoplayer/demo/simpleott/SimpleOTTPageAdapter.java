package com.theoplayer.demo.simpleott;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

class SimpleOTTPageAdapter extends PagerAdapter {

    private final Context mContext;
    private int countInitialized = 0;
    private OnItemsReadyListener onItemsReadyListener;

    public SimpleOTTPageAdapter(Context context) {
        mContext = context;
    }

    public void setOnItemsReady(OnItemsReadyListener listener) {
        onItemsReadyListener = listener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        PageModel modelObject = PageModel.values()[position];
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(modelObject.getLayoutResId(), collection, false);
        collection.addView(layout);
        countInitialized++;
        if (countInitialized >= getCount() && onItemsReadyListener != null) {
            onItemsReadyListener.onItemsReady();
        }
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return PageModel.values().length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        PageModel customPagerEnum = PageModel.values()[position];
        return mContext.getString(customPagerEnum.getTitleResId());
    }

    interface OnItemsReadyListener {
        void onItemsReady();
    }
}

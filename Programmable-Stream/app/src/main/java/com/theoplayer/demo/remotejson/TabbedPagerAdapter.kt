package com.theoplayer.demo.remotejson

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.viewpager.widget.PagerAdapter

internal class TabbedPagerAdapter(private val context: Context) : PagerAdapter() {
    private val tabs = arrayOf(
        Tab(R.string.tabTime, R.layout.time_tab_layout),
        Tab(R.string.tabTracks, R.layout.tracks_tab_layout),
        Tab(R.string.tabState, R.layout.state_tab_layout),
        Tab(R.string.tabAds, R.layout.ads_tab_layout)
    )

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(tabs[position].layoutResId, collection, false) as ViewGroup
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount() = tabs.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int) = context.getString(tabs[position].titleResId)

    private class Tab constructor(
        @field:StringRes @param:StringRes val titleResId: Int,
        @field:LayoutRes @param:LayoutRes val layoutResId: Int
    )
}
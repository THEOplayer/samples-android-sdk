package com.theoplayer.demo.simpleott.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.util.Supplier
import androidx.viewpager.widget.PagerAdapter

class TabbedPagerAdapter(private val context: Context) : PagerAdapter() {
    private val tabs: MutableList<Tab>

    init {
        tabs = ArrayList()
    }

    /**
     * Allows to add new tab to the tabbed view pager.
     *
     * @param titleResId - string resource ID of tab title
     * @param viewBinder - supplier that binds and inflates tab view.
     */
    fun addTab(@StringRes titleResId: Int, viewBinder: Supplier<View>) {
        tabs.add(Tab(titleResId, viewBinder))
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = tabs[position].viewBinder.get()
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun getCount(): Int {
        return tabs.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(tabs[position].titleResId)
    }

    private class Tab internal constructor(
        @field:StringRes @param:StringRes val titleResId: Int,
        val viewBinder: Supplier<View>
    )
}
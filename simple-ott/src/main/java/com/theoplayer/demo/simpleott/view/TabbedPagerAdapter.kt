package com.theoplayer.demo.simpleott.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.util.Supplier
import androidx.viewpager.widget.PagerAdapter

class TabbedPagerAdapter(private val context: Context) : PagerAdapter() {
    private val tabs = mutableListOf<Tab>()

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

    override fun getCount(): Int = tabs.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

    override fun getPageTitle(position: Int): CharSequence = context.getString(tabs[position].titleResId)

    private class Tab(@StringRes val titleResId: Int, val viewBinder: Supplier<View>)
}

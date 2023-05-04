package com.theoplayer.demo.simpleott

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

internal class SimpleOTTPageAdapter(private val mContext: Context) : PagerAdapter() {
    private var countInitialized = 0
    private var onItemsReadyListener: OnItemsReadyListener? = null
    fun setOnItemsReady(listener: OnItemsReadyListener?) {
        onItemsReadyListener = listener
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val modelObject = PageModel.values()[position]
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(modelObject.layoutResId, collection, false) as ViewGroup
        collection.addView(layout)
        countInitialized++
        if (countInitialized >= count && onItemsReadyListener != null) {
            onItemsReadyListener!!.onItemsReady()
        }
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return PageModel.values().size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val customPagerEnum = PageModel.values()[position]
        return mContext.getString(customPagerEnum.titleResId)
    }

    internal interface OnItemsReadyListener {
        fun onItemsReady()
    }
}
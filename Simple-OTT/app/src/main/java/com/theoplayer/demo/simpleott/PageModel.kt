package com.theoplayer.demo.simpleott

enum class PageModel(val titleResId: Int, val layoutResId: Int) {
    LIVE(R.string.tabLive, R.layout.live),
    VOD(R.string.tabOnDemand, R.layout.vod),
    OFFLINE(R.string.tabDownloads, R.layout.offline),
    SETTINGS(R.string.tabSettings, R.layout.settings);
}
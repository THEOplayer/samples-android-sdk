package com.theoplayer.demo.remotejson

enum class ModelObject(val titleResId: Int, val layoutResId: Int) {
    TIME(R.string.tabTime, R.layout.time_tab_layout),
    TRACKS(R.string.tabTracks, R.layout.tracks_tab_layout),
    STATE(R.string.tabState, R.layout.state_tab_layout),
    ADS(R.string.tabAds, R.layout.ads_tab_layout);
}
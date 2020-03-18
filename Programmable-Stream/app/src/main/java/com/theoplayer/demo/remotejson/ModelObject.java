package com.theoplayer.demo.remotejson;

public enum ModelObject {

    TIME(R.string.tabTime, R.layout.time_tab_layout),
    TRACKS(R.string.tabTracks, R.layout.tracks_tab_layout),
    STATE(R.string.tabState, R.layout.state_tab_layout),
    ADS(R.string.tabAds, R.layout.ads_tab_layout);

    private final int mTitleResId;
    private final int mLayoutResId;

    ModelObject(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}


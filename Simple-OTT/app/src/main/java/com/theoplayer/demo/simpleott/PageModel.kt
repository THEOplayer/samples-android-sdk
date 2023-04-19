package com.theoplayer.demo.simpleott;

public enum PageModel {

    LIVE(R.string.tabLive, R.layout.live),
    VOD(R.string.tabOnDemand, R.layout.vod),
    OFFLINE(R.string.tabDownloads, R.layout.offline),
    SETTINGS(R.string.tabSettings, R.layout.settings);

    private final int titleResId;
    private final int layoutResId;

    PageModel(int titleResId, int layoutResId) {
        this.titleResId = titleResId;
        this.layoutResId = layoutResId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getLayoutResId() {
        return layoutResId;
    }
}

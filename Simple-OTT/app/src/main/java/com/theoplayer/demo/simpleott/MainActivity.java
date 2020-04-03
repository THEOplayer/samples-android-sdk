
package com.theoplayer.demo.simpleott;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.theoplayer.demo.simpleott.databinding.ActivityMainBinding;
import com.theoplayer.demo.simpleott.databinding.TabOfflineSourceBinding;
import com.theoplayer.demo.simpleott.databinding.TabSettingsBinding;
import com.theoplayer.demo.simpleott.databinding.TabStreamSourceBinding;
import com.theoplayer.demo.simpleott.model.OfflineSourceDownloader;
import com.theoplayer.demo.simpleott.model.StreamSourceRepository;
import com.theoplayer.demo.simpleott.network.WiFiNetworkInfo;
import com.theoplayer.demo.simpleott.view.OfflineSourceAdapter;
import com.theoplayer.demo.simpleott.view.StreamSourceAdapter;
import com.theoplayer.demo.simpleott.view.TabbedPagerAdapter;

import static android.view.LayoutInflater.from;

public class MainActivity extends AppCompatActivity {

    private WiFiNetworkInfo wiFiNetworkInfo;
    private StreamSourceRepository streamSourceRepository;
    private OfflineSourceDownloader offlineSourceDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.TheoTheme_Base);
        super.onCreate(savedInstanceState);

        // Inflating view and obtaining an instance of the binding class.
        ActivityMainBinding viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Initiating WiFi network information provider.
        wiFiNetworkInfo = new WiFiNetworkInfo(this);

        // Initiating repositories that allow to get needed stream sources lists.
        streamSourceRepository = new StreamSourceRepository(this);
        offlineSourceDownloader = new OfflineSourceDownloader(this, streamSourceRepository, wiFiNetworkInfo);

        // Initializing pager adapter for tabs view
        TabbedPagerAdapter pagerAdapter = new TabbedPagerAdapter(this);
        pagerAdapter.addTab(R.string.tabLive, this::bindLiveTabView);
        pagerAdapter.addTab(R.string.tabOnDemand, this::bindOnDemandTabView);
        pagerAdapter.addTab(R.string.tabOffline, this::bindOfflineTabView);
        pagerAdapter.addTab(R.string.tabSettings, this::bindSettingsTabView);

        viewBinding.viewPager.setAdapter(pagerAdapter);
        viewBinding.viewPager.setOffscreenPageLimit(4);
    }

    /**
     * Inflates and binds view for LIVE tab.
     *
     * @return bound view
     */
    private View bindLiveTabView() {
        TabStreamSourceBinding viewBinding = TabStreamSourceBinding.inflate(from(this), null, false);

        viewBinding.streamSourceList.setAdapter(
                new StreamSourceAdapter(this, streamSourceRepository.getLiveStreamSources())
        );

        return viewBinding.getRoot();
    }

    /**
     * Inflates and binds view for ON DEMAND tab.
     *
     * @return bound view
     */
    private View bindOnDemandTabView() {
        TabStreamSourceBinding viewBinding = TabStreamSourceBinding.inflate(from(this), null, false);

        viewBinding.streamSourceList.setAdapter(
                new StreamSourceAdapter(this, streamSourceRepository.getOnDemandStreamSources())
        );

        return viewBinding.getRoot();
    }

    /**
     * Inflates nad binds view for OFFLINE tab.
     *
     * @return bound view
     */
    private View bindOfflineTabView() {
        TabOfflineSourceBinding viewBinding = TabOfflineSourceBinding.inflate(from(this), null, false);

        viewBinding.offlineSourceList.setAdapter(
                new OfflineSourceAdapter(this, offlineSourceDownloader)
        );

        return viewBinding.getRoot();
    }

    /**
     * Inflates and binds view for SETTINGS tab.
     *
     * @return bound view
     */
    private View bindSettingsTabView() {
        TabSettingsBinding viewBinding = TabSettingsBinding.inflate(from(this), null, false);

        // Showing confirmation dialog after hitting clear cache button
        viewBinding.removeAllButton.setOnClickListener(v -> offlineSourceDownloader.removeAllCachingTasks());

        // The switch for "Download only on wifi" setting
        viewBinding.downloadOnWiFiSwitch.setChecked(wiFiNetworkInfo.isDownloadOnlyOnWiFi());
        viewBinding.downloadOnWiFiSwitch.setOnCheckedChangeListener(
                (button, isChecked) -> wiFiNetworkInfo.setDownloadOnlyOnWiFi(isChecked));

        return viewBinding.getRoot();
    }

}

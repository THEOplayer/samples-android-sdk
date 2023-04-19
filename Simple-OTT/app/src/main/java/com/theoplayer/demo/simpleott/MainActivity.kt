package com.theoplayer.demo.simpleott

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.theoplayer.demo.simpleott.databinding.ActivityMainBinding
import com.theoplayer.demo.simpleott.databinding.TabOfflineSourceBinding
import com.theoplayer.demo.simpleott.databinding.TabSettingsBinding
import com.theoplayer.demo.simpleott.databinding.TabStreamSourceBinding
import com.theoplayer.demo.simpleott.model.OfflineSourceDownloader
import com.theoplayer.demo.simpleott.model.StreamSourceRepository
import com.theoplayer.demo.simpleott.network.WiFiNetworkInfo
import com.theoplayer.demo.simpleott.view.OfflineSourceAdapter
import com.theoplayer.demo.simpleott.view.StreamSourceAdapter
import com.theoplayer.demo.simpleott.view.TabbedPagerAdapter

class MainActivity : AppCompatActivity() {
    private var wiFiNetworkInfo: WiFiNetworkInfo? = null
    private var streamSourceRepository: StreamSourceRepository? = null
    private var offlineSourceDownloader: OfflineSourceDownloader? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TheoTheme_Base)
        super.onCreate(savedInstanceState)

        // Inflating view and obtaining an instance of the binding class.
        val viewBinding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        // Initiating WiFi network information provider.
        wiFiNetworkInfo = WiFiNetworkInfo(this)

        // Initiating repositories that allow to get needed stream sources lists.
        streamSourceRepository = StreamSourceRepository(this)
        offlineSourceDownloader =
            OfflineSourceDownloader(this, streamSourceRepository!!, wiFiNetworkInfo!!)

        // Initializing pager adapter for tabs view
        val pagerAdapter = TabbedPagerAdapter(this)
        pagerAdapter.addTab(R.string.tabLive) { bindLiveTabView() }
        pagerAdapter.addTab(R.string.tabOnDemand) { bindOnDemandTabView() }
        pagerAdapter.addTab(R.string.tabOffline) { bindOfflineTabView() }
        pagerAdapter.addTab(R.string.tabSettings) { bindSettingsTabView() }
        viewBinding.viewPager.adapter = pagerAdapter
        viewBinding.viewPager.offscreenPageLimit = 4
    }

    /**
     * Inflates and binds view for LIVE tab.
     *
     * @return bound view
     */
    private fun bindLiveTabView(): View {
        val viewBinding = TabStreamSourceBinding.inflate(LayoutInflater.from(this), null, false)
        viewBinding.streamSourceList.adapter =
            StreamSourceAdapter(this, streamSourceRepository.getLiveStreamSources())
        return viewBinding.root
    }

    /**
     * Inflates and binds view for ON DEMAND tab.
     *
     * @return bound view
     */
    private fun bindOnDemandTabView(): View {
        val viewBinding = TabStreamSourceBinding.inflate(LayoutInflater.from(this), null, false)
        viewBinding.streamSourceList.adapter =
            StreamSourceAdapter(this, streamSourceRepository.getOnDemandStreamSources())
        return viewBinding.root
    }

    /**
     * Inflates nad binds view for OFFLINE tab.
     *
     * @return bound view
     */
    private fun bindOfflineTabView(): View {
        val viewBinding = TabOfflineSourceBinding.inflate(LayoutInflater.from(this), null, false)
        viewBinding.offlineSourceList.adapter = OfflineSourceAdapter(this, offlineSourceDownloader)
        return viewBinding.root
    }

    /**
     * Inflates and binds view for SETTINGS tab.
     *
     * @return bound view
     */
    private fun bindSettingsTabView(): View {
        val viewBinding = TabSettingsBinding.inflate(LayoutInflater.from(this), null, false)

        // Showing confirmation dialog after hitting clear cache button
        viewBinding.removeAllButton.setOnClickListener { v: View? -> offlineSourceDownloader!!.removeAllCachingTasks() }

        // The switch for "Download only on wifi" setting
        viewBinding.downloadOnWiFiSwitch.isChecked = wiFiNetworkInfo!!.isDownloadOnlyOnWiFi
        viewBinding.downloadOnWiFiSwitch.setOnCheckedChangeListener { button: CompoundButton?, isChecked: Boolean ->
            wiFiNetworkInfo!!.isDownloadOnlyOnWiFi = isChecked
        }
        return viewBinding.root
    }
}
package com.theoplayer.demo.simpleott

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.theoplayer.demo.simpleott.databinding.ActivityMainBinding
import com.theoplayer.demo.simpleott.databinding.TabOfflineSourceBinding
import com.theoplayer.demo.simpleott.databinding.TabStreamSourceBinding
import com.theoplayer.demo.simpleott.model.OfflineSourceDownloader
import com.theoplayer.demo.simpleott.model.StreamSource
import com.theoplayer.demo.simpleott.network.WiFiNetworkInfo
import com.theoplayer.demo.simpleott.view.OfflineSourceAdapter
import com.theoplayer.demo.simpleott.view.StreamSourceAdapter
import com.theoplayer.demo.simpleott.view.TabbedPagerAdapter
import com.theoplayer.sample.common.AppTopBar
import com.theoplayer.sample.common.SourceManager

class MainActivity : AppCompatActivity() {
    private lateinit var wiFiNetworkInfo: WiFiNetworkInfo
    private lateinit var offlineSourceDownloader: OfflineSourceDownloader

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DolbyTheme_SimpleOTT)
        super.onCreate(savedInstanceState)

        val viewBinding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        // Set up the toolbar
        viewBinding.composeToolbar.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { AppTopBar() }
        }

        // Initiating WiFi network information provider.
        wiFiNetworkInfo = WiFiNetworkInfo(this)
        offlineSourceDownloader = OfflineSourceDownloader(this, wiFiNetworkInfo)

        // Initializing pager adapter for tabs view
        val pagerAdapter = TabbedPagerAdapter(this)
        pagerAdapter.addTab(R.string.tabOnDemand) { bindOnDemandTabView() }
        pagerAdapter.addTab(R.string.tabLive) { bindLiveTabView() }
        pagerAdapter.addTab(R.string.tabOffline) { bindOfflineTabView() }
        viewBinding.viewPager.adapter = pagerAdapter
        viewBinding.viewPager.offscreenPageLimit = 3

        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager)
    }

    /**
     * Inflates and binds view for LIVE tab.
     *
     * @return bound view
     */
    private fun bindLiveTabView(): View {
        val viewBinding = TabStreamSourceBinding.inflate(LayoutInflater.from(this), null, false)
        viewBinding.streamSourceList.layoutManager = LinearLayoutManager(this)
        viewBinding.streamSourceList.adapter = StreamSourceAdapter(LIVE_SOURCES)
        return viewBinding.root
    }

    /**
     * Inflates and binds view for ON DEMAND tab.
     *
     * @return bound view
     */
    private fun bindOnDemandTabView(): View {
        val viewBinding = TabStreamSourceBinding.inflate(LayoutInflater.from(this), null, false)
        viewBinding.streamSourceList.layoutManager = LinearLayoutManager(this)
        viewBinding.streamSourceList.adapter = StreamSourceAdapter(ON_DEMAND_SOURCES)
        return viewBinding.root
    }

    /**
     * Inflates nad binds view for OFFLINE tab.
     *
     * @return bound view
     */
    private fun bindOfflineTabView(): View {
        val viewBinding = TabOfflineSourceBinding.inflate(LayoutInflater.from(this), null, false)

        // WiFi setting
        viewBinding.downloadOnWiFiSwitch.isChecked = wiFiNetworkInfo.isDownloadOnlyOnWiFi
        viewBinding.downloadOnWiFiSwitch.setOnCheckedChangeListener { _, isChecked ->
            wiFiNetworkInfo.setDownloadOnlyOnWiFi(isChecked)
        }

        // Clear all downloads button
        viewBinding.removeAllButton.setOnClickListener {
            offlineSourceDownloader.removeAllCachingTasks()
        }

        // Offline sources list
        viewBinding.offlineSourceList.layoutManager = LinearLayoutManager(this)
        viewBinding.offlineSourceList.adapter = OfflineSourceAdapter(
            offlineSourceDownloader.offlineSources,
            { offlineSource -> offlineSourceDownloader.startCachingTask(offlineSource) },
            { offlineSource -> offlineSourceDownloader.pauseCachingTask(offlineSource) },
            { offlineSource -> offlineSourceDownloader.removeCachingTask(offlineSource) },
            { offlineSource -> PlayerActivity.play(this, offlineSource.sourceUrl, offlineSource.title) }
        )
        return viewBinding.root
    }

    companion object {
        val LIVE_SOURCES = listOf(
            StreamSource(
                "Channel 1",
                "LIVE",
                SourceManager.STAR_WARS_HLS.sources[0].src,
                R.drawable.image_live
            ),
            StreamSource(
                "Channel 2",
                "LIVE",
                SourceManager.BIG_BUCK_BUNNY_HLS.sources[0].src,
                R.drawable.image_live
            )
        )

        val ON_DEMAND_SOURCES = listOf(
            StreamSource(
                "Big Buck Bunny",
                "2008 \u2027 Short/Comedy \u2027 12 mins",
                SourceManager.BIG_BUCK_BUNNY_HLS.sources[0].src,
                R.drawable.image_big_buck_bunny
            ),
            StreamSource(
                "Sintel",
                "2010 \u2027 Fantasy/Short \u2027 15 mins",
                SourceManager.SINTEL_HLS.sources[0].src,
                R.drawable.image_sintel
            ),
            StreamSource(
                "Tears of Steel",
                "2012 \u2027 Short/Sci-fi \u2027 12 mins",
                SourceManager.TEARS_OF_STEEL_HLS.sources[0].src,
                R.drawable.image_tears_of_steel
            ),
            StreamSource(
                "Elephant's Dream",
                "2006 \u2027 Sci-fi/Short \u2027 11 mins",
                SourceManager.ELEPHANTS_DREAM_HLS.sources[0].src,
                R.drawable.image_elephants_dream
            ),
            StreamSource(
                "Cosmos",
                "2013 \u2027 Short \u2027 12 mins",
                SourceManager.COSMOS_DASH.sources[0].src,
                R.drawable.image_caminandes_llama_drama
            )
        )
    }
}

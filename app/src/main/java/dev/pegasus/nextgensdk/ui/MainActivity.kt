package dev.pegasus.nextgensdk.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import dev.pegasus.nextgensdk.databinding.ActivityMainBinding
import dev.pegasus.nextgensdk.ui.entrance.EntranceActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initAdmobSdk()
    }

    private fun initAdmobSdk() {
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.Companion.initialize(this@MainActivity, InitializationConfig.Builder("ca-app-pub-3940256099942544~3347511713").build()) {
                // Ensure setupAds runs on main thread (lifecycle operations require main thread)
                lifecycleScope.launch(Dispatchers.Main) {
                    setupAds()
                }
            }
        }
    }

    private fun setupAds() {
        startActivity(Intent(this, EntranceActivity::class.java))
        finish()
    }
}
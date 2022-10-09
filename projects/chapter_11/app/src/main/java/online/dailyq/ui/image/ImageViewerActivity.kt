package online.dailyq.ui.image

import android.os.Bundle
import android.view.MenuItem
import coil.load
import online.dailyq.R
import online.dailyq.databinding.ActivityImageViewerBinding
import online.dailyq.ui.base.BaseActivity

class ImageViewerActivity : BaseActivity() {
    companion object {
        const val EXTRA_URL = "url"
    }

    lateinit var binding: ActivityImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val url = intent.getStringExtra(EXTRA_URL)
        binding.image.load(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

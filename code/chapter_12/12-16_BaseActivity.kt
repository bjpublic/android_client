package online.dailyq.ui.base

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import online.dailyq.R
import online.dailyq.Settings
import online.dailyq.api.ApiService
import online.dailyq.db.AppDatabase
import online.dailyq.ui.image.ImageViewerActivity

abstract class BaseActivity : AppCompatActivity() {
    val api: ApiService by lazy { ApiService.getInstance() }
    val db: AppDatabase by lazy { AppDatabase.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Settings.theme == Settings.Theme.Dark) {
            if (this !is ImageViewerActivity) {
                setTheme(R.style.Theme_DailyQ_Dark)
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

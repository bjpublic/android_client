package online.dailyq.ui.base

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import online.dailyq.api.ApiService

abstract class BaseActivity : AppCompatActivity() {
    val api: ApiService by lazy { ApiService.getInstance() }

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

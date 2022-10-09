package online.dailyq.ui.theme

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import online.dailyq.R
import online.dailyq.Settings
import online.dailyq.databinding.ActivityThemeBinding
import online.dailyq.ui.base.BaseActivity
import online.dailyq.ui.main.MainActivity

class ThemeActivity : BaseActivity() {

    lateinit var binding: ActivityThemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val buttonId = when (Settings.theme) {
            Settings.Theme.Dark -> R.id.dark_theme
            else -> R.id.light_theme
        }
        binding.themePreviews.check(buttonId)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.theme_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.done -> {
                Settings.theme = when (binding.themePreviews.checkedRadioButtonId) {
                    R.id.dark_theme -> Settings.Theme.Dark
                    else -> Settings.Theme.Light
                }
                startActivity(Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}


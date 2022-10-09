package online.dailyq.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.dailyq.AuthManager
import online.dailyq.R
import online.dailyq.Settings
import online.dailyq.databinding.ActivityMainBinding
import online.dailyq.ui.base.BaseActivity
import online.dailyq.ui.profile.ProfileFragment
import online.dailyq.ui.splash.SplashActivity
import online.dailyq.ui.theme.ThemeActivity
import online.dailyq.ui.timeline.TimelineFragment
import online.dailyq.ui.today.TodayFragment

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.setOnItemSelectedListener {
            val ft = supportFragmentManager.beginTransaction()

            when (it.itemId) {
                R.id.timeline -> {
                    ft.replace(R.id.host, TimelineFragment())
                    supportActionBar?.setTitle(R.string.title_timeline)
                }
                R.id.today -> {
                    ft.replace(R.id.host, TodayFragment())
                    supportActionBar?.setTitle(R.string.title_today)
                }
                R.id.profile -> {
                    ft.replace(R.id.host, ProfileFragment().apply {
                        arguments = Bundle().apply {
                            putString(ProfileFragment.ARG_UID, AuthManager.uid)
                        }
                    })
                    supportActionBar?.setTitle(R.string.title_profile)
                }
            }
            ft.commit()
            true
        }

        binding.navView.selectedItemId = R.id.today
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.theme -> {
                startActivity(Intent(this@MainActivity, ThemeActivity::class.java))
                return true
            }
            R.id.logout -> {
                lifecycleScope.launch {
                    AuthManager.clear()
                    Settings.clear()

                    withContext(Dispatchers.IO) {
                        db.clearAllTables()
                    }

                    startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

package online.dailyq.ui.base

import androidx.fragment.app.Fragment
import online.dailyq.api.ApiService
import online.dailyq.db.AppDatabase

abstract class BaseFragment : Fragment() {
    val api: ApiService by lazy { ApiService.getInstance() }
    val db: AppDatabase by lazy { AppDatabase.getInstance() }
}

package online.dailyq.ui.base

import androidx.fragment.app.Fragment
import online.dailyq.api.ApiService

abstract class BaseFragment : Fragment() {
    val api: ApiService by lazy { ApiService.getInstance() }

}

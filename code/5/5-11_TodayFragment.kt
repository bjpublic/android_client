package online.dailyq.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import online.dailyq.databinding.FragmentTodayBinding
import online.dailyq.ui.base.BaseFragment
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : BaseFragment() {

    var _binding: FragmentTodayBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val qidDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val qid = qidDateFormat.format(Date())
            val question = api.getQuestion(qid)

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.KOREA)
            binding.date.text = dateFormat.format(qidDateFormat.parse(question.id))
            binding.question.text = question.text
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}

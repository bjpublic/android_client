package online.dailyq.ui.today

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import online.dailyq.R
import online.dailyq.api.response.Question
import online.dailyq.databinding.FragmentTodayBinding
import online.dailyq.ui.base.BaseFragment
import online.dailyq.ui.write.WriteActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : BaseFragment() {

    var _binding: FragmentTodayBinding? = null
    val binding get() = _binding!!

    var question: Question? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.writeButton.setOnClickListener {
            startActivity(Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra(WriteActivity.EXTRA_QID, question!!.id)
                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.WRITE)
            })
        }
        binding.editButton.setOnClickListener {
            startActivity(Intent(requireContext(), WriteActivity::class.java).apply {
                putExtra(WriteActivity.EXTRA_QID, question!!.id)
                putExtra(WriteActivity.EXTRA_MODE, WriteActivity.Mode.EDIT)
            })
        }
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val qidDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val qid = qidDateFormat.format(Date())

            val questionResponse = api.getQuestion(qid)
            if (questionResponse.isSuccessful) {
                question = questionResponse.body()!!

                val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.KOREA)
                binding.date.text = dateFormat.format(qidDateFormat.parse(question!!.id))
                binding.question.text = question!!.text

                val answer = api.getAnswer(question!!.id).body()
                binding.answerArea.isVisible = answer != null
                binding.textAnswer.text = answer?.text

                binding.writeButton.isVisible = answer == null
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.dialog_msg_are_you_sure_to_delete)
            .setPositiveButton(R.string.ok) { dialog, which ->
                lifecycleScope.launch {
                    val deleteResponse = api.deleteAnswer(question!!.id)
                    if (deleteResponse.isSuccessful) {
                        binding.answerArea.isVisible = false
                        binding.writeButton.isVisible = true
                    }
                }
            }.setNegativeButton(R.string.cancel) { dialog, which ->

            }.show()
    }


}

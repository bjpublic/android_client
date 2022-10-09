package online.dailyq.ui.write

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import online.dailyq.R
import online.dailyq.api.response.Answer
import online.dailyq.api.response.Question
import online.dailyq.databinding.ActivityWriteBinding
import online.dailyq.ui.base.BaseActivity
import java.text.DateFormat
import java.text.SimpleDateFormat

class WriteActivity : BaseActivity() {
    companion object {
        const val EXTRA_QID = "qid"
        const val EXTRA_MODE = "mode"
    }

    enum class Mode {
        WRITE, EDIT
    }

    lateinit var binding: ActivityWriteBinding
    lateinit var mode: Mode

    lateinit var question: Question
    var answer: Answer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val qidDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val qid = intent.getStringExtra(EXTRA_QID)!!
        mode = intent?.getSerializableExtra(EXTRA_MODE)!! as Mode

        supportActionBar?.title = DateFormat.getDateInstance(DateFormat.LONG)
            .format(qidDateFormat.parse(qid))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            question = api.getQuestion(qid).body()!!
            answer = api.getAnswer(qid).body()

            binding.question.text = question.text
            binding.answer.setText(answer?.text)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.write_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.done -> {
                write()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun write() {
        val text = binding.answer.text.toString().trimEnd()
        lifecycleScope.launch {
            val answerResponse = if (answer == null) {
                api.writeAnswer(question.id, text)
            } else {
                api.editAnswer(question.id, text)
            }
            if (answerResponse.isSuccessful) {
                finish()
            } else {
                Toast.makeText(
                    this@WriteActivity,
                    answerResponse.message(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

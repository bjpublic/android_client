package online.dailyq.ui.details

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import online.dailyq.R
import online.dailyq.databinding.ActivityDetailsBinding
import online.dailyq.db.entity.QuestionEntity
import online.dailyq.ui.base.BaseActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetailsActivity : BaseActivity() {
    companion object {
        const val EXTRA_QID = "qid"
    }

    lateinit var binding: ActivityDetailsBinding
    var adapter: AnswerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val qid = intent?.getSerializableExtra(EXTRA_QID) as LocalDate

        supportActionBar?.title = DateTimeFormatter.ofPattern(getString(R.string.date_format)).format(qid)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = AnswerAdapter(this)
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                val itemCount = adapter?.items?.size ?: 0
                binding.empty.isVisible = itemCount == 0
            }
        })

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        lifecycleScope.launch {
            db.getQuestionDao().get(qid.toString())?.let {
                binding.question.text = it.text
            }

            val questionResponse = api.getQuestion(qid)

            if (questionResponse.isSuccessful) {
                val question = questionResponse.body()
                binding.question.text = question?.text

                question?.let {
                    val questionEntity = QuestionEntity(it.id, it.text, it.answerCount, it.updatedAt, it.createdAt)
                    db.getQuestionDao().insertOrReplace(questionEntity)
                }
            }

            val answersResponse = api.getAnswers(qid)
            if (answersResponse.isSuccessful) {
                adapter?.items = answersResponse.body()
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

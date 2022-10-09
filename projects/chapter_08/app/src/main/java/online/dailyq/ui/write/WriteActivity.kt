package online.dailyq.ui.write

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.RoundedCornersTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import online.dailyq.R
import online.dailyq.api.asRequestBody
import online.dailyq.api.response.Answer
import online.dailyq.api.response.Question
import online.dailyq.databinding.ActivityWriteBinding
import online.dailyq.ui.base.BaseActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    var imageUrl: String? = null

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    val imageUri = result.data?.data ?: return@launch
                    val requestBody = imageUri.asRequestBody(contentResolver)

                    val part = MultipartBody.Part.createFormData("image", "filename", requestBody)
                    val imageResponse = api.uploadImage(part)

                    if (imageResponse.isSuccessful) {
                        imageUrl = imageResponse.body()!!.url

                        binding.photo.load(imageUrl) {
                            transformations(RoundedCornersTransformation(resources.getDimension(R.dimen.thumbnail_rounded_corner)))
                        }
                        binding.photoArea.isVisible = true
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val qid = intent.getSerializableExtra(EXTRA_QID) as LocalDate
        mode = intent?.getSerializableExtra(EXTRA_MODE)!! as Mode

        supportActionBar?.title =
            DateTimeFormatter.ofPattern(getString(R.string.date_format)).format(qid)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            question = api.getQuestion(qid).body()!!
            answer = api.getAnswer(qid).body()

            binding.question.text = question.text
            binding.answer.setText(answer?.text)

            imageUrl = answer?.photo
            binding.photoArea.isVisible = !imageUrl.isNullOrEmpty()

            imageUrl?.let {
                binding.photo.load(it) {
                    transformations(RoundedCornersTransformation(resources.getDimension(R.dimen.thumbnail_rounded_corner)))
                }
            }
        }

        binding.photoArea.setOnClickListener {
            showDeleteConfirmDialog()
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
            R.id.add_photo -> {
                startForResult.launch(
                    Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/*"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
                    })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun write() {
        val text = binding.answer.text.toString().trimEnd()
        lifecycleScope.launch {
            val answerResponse = if (answer == null) {
                api.writeAnswer(question.id, text, imageUrl)
            } else {
                api.editAnswer(question.id, text, imageUrl)
            }
            if (answerResponse.isSuccessful) {
                setResult(RESULT_OK)
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

    fun showDeleteConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.dialog_msg_are_you_sure_to_delete)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                binding.photo.setImageResource(0)
                binding.photoArea.isVisible = false
                imageUrl = null
            }.setNegativeButton(android.R.string.cancel) { dialog, which ->

            }.show()
    }
}

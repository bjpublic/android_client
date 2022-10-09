package online.dailyq.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import online.dailyq.api.response.QuestionAndAnswer
import online.dailyq.databinding.ItemUserAnswerCardBinding

class UserAnswerAdapter(context: Context) :
    PagingDataAdapter<QuestionAndAnswer, UserAnswerViewHolder>(
        QuestionResponseDiffCallback
    ) {

    object QuestionResponseDiffCallback : DiffUtil.ItemCallback<QuestionAndAnswer>() {
        override fun areItemsTheSame(
            oldItem: QuestionAndAnswer,
            newItem: QuestionAndAnswer
        ): Boolean {
            return oldItem.question.id == newItem.question.id
        }

        override fun areContentsTheSame(
            oldItem: QuestionAndAnswer,
            newItem: QuestionAndAnswer
        ): Boolean {
            return oldItem == newItem
        }
    }

    val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAnswerViewHolder {
        return UserAnswerViewHolder(ItemUserAnswerCardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: UserAnswerViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

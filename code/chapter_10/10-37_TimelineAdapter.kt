package online.dailyq.ui.timeline

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import online.dailyq.api.response.Question
import online.dailyq.databinding.ItemTimelineCardBinding
import online.dailyq.db.entity.QuestionEntity


class TimelineAdapter(val context: Context) :
    PagingDataAdapter<QuestionEntity, TimelineCardViewHolder>(QuestionComparator) {

    object QuestionComparator: DiffUtil.ItemCallback<QuestionEntity>() {
        override fun areItemsTheSame(
            oldItem: QuestionEntity,
            newItem: QuestionEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: QuestionEntity,
            newItem: QuestionEntity
        ): Boolean {
            return oldItem == newItem
        }
    }

    val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineCardViewHolder {
        return TimelineCardViewHolder(ItemTimelineCardBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: TimelineCardViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

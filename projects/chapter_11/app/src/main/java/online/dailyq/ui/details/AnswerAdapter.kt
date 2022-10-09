package online.dailyq.ui.details

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import online.dailyq.api.response.Answer
import online.dailyq.databinding.ItemAnswerBinding

class AnswerAdapter(context: Context) : RecyclerView.Adapter<AnswerViewHolder>() {

    val inflater = LayoutInflater.from(context)

    var items: List<Answer>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        return AnswerViewHolder(ItemAnswerBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        holder.bind(items!![position])
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }
}

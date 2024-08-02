package io.agora.transcription_widget_demo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.agora.transcription_widget_demo.R

class MultiSelectAdapter(
    private val items: List<String>,
    private val selectedItems: MutableSet<String>,
    private val isMultiSelect: Boolean = true
) : RecyclerView.Adapter<MultiSelectAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_text)
        val checkMark: ImageView = itemView.findViewById(R.id.check_mark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_multi_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textView.text = item
        if (isMultiSelect) {
            holder.checkMark.visibility =
                if (selectedItems.contains(item)) View.VISIBLE else View.GONE
        } else {
            holder.checkMark.visibility = selectedItems.firstOrNull { it == item }.let {
                if (it != null) View.VISIBLE else View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            if (isMultiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item)
                    holder.checkMark.visibility = View.GONE
                } else {
                    selectedItems.add(item)
                    holder.checkMark.visibility = View.VISIBLE
                }
            } else {
                selectedItems.clear()
                selectedItems.add(item)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
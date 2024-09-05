package com.familypedia.view.dashboard.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.familypedia.R
import com.familypedia.databinding.ItemRecentSearchBinding
import com.familypedia.utils.listeners.RecentSearchesListener
import com.familypedia.utils.setSafeOnClickListener
import com.familypedia.utils.setTextOnTextView

private const val TAG = "RecentSearchAdapter"

class RecentSearchAdapter(
    private val context: Context,
    private val dataList: ArrayList<String?>?,
    private val listener: RecentSearchesListener
) : RecyclerView.Adapter<RecentSearchAdapter.RecentSearchesViewHolder>() {

    inner class RecentSearchesViewHolder(private val binding: ItemRecentSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(searchText: String) {
            binding.tvSearchText.setTextOnTextView(searchText, "")
            Log.d(TAG, "bind: $searchText")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchesViewHolder {
        val binding = ItemRecentSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecentSearchesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentSearchesViewHolder, position: Int) {
        val searchText = dataList?.get(position) ?: ""
        holder.bind(searchText)
        holder.itemView.setSafeOnClickListener {
            listener.onRecentSearchClick(searchText)
        }
    }

    override fun getItemCount(): Int {
        return dataList?.size?.coerceAtMost(5) ?: 0
    }
}

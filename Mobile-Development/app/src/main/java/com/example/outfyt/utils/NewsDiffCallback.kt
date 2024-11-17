package com.example.outfyt.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.outfyt.data.remote.response.DataItem

class NewsDiffCallback(
    private val oldList: List<DataItem>,
    private val newList: List<DataItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].title == newList[newItemPosition].title
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
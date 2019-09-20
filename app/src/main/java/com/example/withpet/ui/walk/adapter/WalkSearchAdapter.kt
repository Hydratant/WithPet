package com.example.withpet.ui.walk.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.withpet.core.adapter.SingleAdapter
import com.example.withpet.databinding.WalkInfoDetailItemBinding
import com.example.withpet.databinding.WalkSearchFragmentItemBinding
import com.example.withpet.generated.callback.OnClickListener
import com.example.withpet.util.Log
import com.example.withpet.vo.walk.WalkBaseDTO
import com.example.withpet.vo.walk.WalkDetailRowDTO


class WalkSearchAdapter(private val onItemClickListener: (WalkBaseDTO) -> Unit) : SingleAdapter<WalkSearchAdapter.WalkSearchHolder, WalkBaseDTO>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkSearchHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val binding = WalkSearchFragmentItemBinding.inflate(inflater, parent, false)
        return WalkSearchHolder(binding)
    }

    override fun onBind(holder: WalkSearchHolder, item: WalkBaseDTO) = holder.bind(item)

    inner class WalkSearchHolder(private val binding: WalkSearchFragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalkBaseDTO) {
            binding.run {
                data = item
                root.setOnClickListener { onItemClickListener.invoke(item) }
            }
        }
    }

}